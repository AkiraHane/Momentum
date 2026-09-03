package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class WallKickState extends BaseState {

    private static final int WALL_KICK_STATE_TICKS = 5;
    private static final int WALL_RUN_REENTRY_GRACE_TICKS = 8;
    private static final double HORIZONTAL_EPSILON = 1.0E-6;
    private static final double WALL_RUN_KICK_NORMAL_MULTIPLIER = 1;

    public static boolean canWallKick(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                !player.onGround() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                context.getInputVec().horizontalDistance() > 0.01 && Mth.abs(context.getInputWallAngle()) >= 120 &&
                checkKey(player, context);
    }

    // 跑墙特殊进入条件
    public static boolean canWallKickRun(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_KICK);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 蹬墙跳持续期自保持（替代 canWallKick 入口检查）
        return withPredicate(DEFAULT_CHAIN, StateType.WALL_KICK, (p, c) -> c.getWallJumpTimer() > 0);
    }


    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        ).normalize();
        Vec3 motionDirection = context.getSpeed().normalize();
        boolean isBackwardJump = (lookVec.x * motionDirection.x + lookVec.z * motionDirection.z) < 0;
        if (context.isLeftFootJump()) {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_LEFT : BaseState.JUMP_LEFT,
                    context);
        } else {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_RIGHT : BaseState.JUMP_RIGHT,
                    context);
        }
        context.setLeftFootJump(!context.isLeftFootJump());
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        boolean fromWallRun = context.getPreviousStateType() == StateType.WALL_RUN;
        boolean inputDirected = Mth.abs(context.getInputWallAngle()) >= 120;
        if (fromWallRun) {
            // 墙跑保持条件允许玩家先转向墙外再起跳。此时沿用修改前的输入导向，
            // 避免固定墙面法线覆盖玩家当前视角/移动输入。
            if (inputDirected) {
                applyInputDirectedKick(player, context, jumpPower);
            } else {
                applyWallRunKick(player, context, jumpPower);
            }
            context.setWallRunReentryGraceTicks(WALL_RUN_REENTRY_GRACE_TICKS);
            if (inputDirected) {
                player.fallDistance = 0;
            }
        } else if (inputDirected) {
            applyInputDirectedKick(player, context, jumpPower);
            player.fallDistance = 0;
        } else {
            player.addDeltaMovement(
                    new Vec3(
                            -context.getWallNormal().x * jumpPower,
                            0,
                            -context.getWallNormal().z * jumpPower
                    )
            );
        }
        context.playWallSound(player, PlayerMovementContext.FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
        context.setWallJumpTimer(WALL_KICK_STATE_TICKS);
    }

    /**
     * 墙跑派生的墙面导向蹬墙跳：保留完整水平动量，并把方向转向墙外。
     * 冷却只禁止增加水平总速度，不再删除已有速度分量。
     */
    private static void applyWallRunKick(Player player, PlayerMovementContext context, float jumpPower) {
        Vec3 normal = context.getRunWallNormal();
        if (normal.horizontalDistanceSqr() < HORIZONTAL_EPSILON) {
            normal = context.getWallNormal();
        }
        normal = new Vec3(normal.x, 0, normal.z).normalize();

        Vec3 current = player.getDeltaMovement();
        Vec3 currentHorizontal = new Vec3(current.x, 0, current.z);
        Vec3 awayFromWall = normal.scale(-jumpPower * WALL_RUN_KICK_NORMAL_MULTIPLIER);
        double ySpeed = Mth.abs(context.getInputWallAngle()) >= 100
                ? jumpPower * 1.5
                : current.y;

        setMomentumPreservingHorizontalVelocity(
                player,
                context,
                currentHorizontal.add(awayFromWall),
                ySpeed,
                jumpPower
        );
    }

    /** 输入型蹬墙跳仍按输入转向，但不再分别重写世界坐标 X/Z。 */
    private static void applyInputDirectedKick(Player player, PlayerMovementContext context, float jumpPower) {
        Vec3 current = player.getDeltaMovement();
        Vec3 currentHorizontal = new Vec3(current.x, 0, current.z);
        Vec3 input = new Vec3(context.getInputVec().x, 0, context.getInputVec().z);
        if (input.horizontalDistance() > 1.0) {
            input = input.normalize();
        }
        Vec3 desiredHorizontal = currentHorizontal.add(input.scale(jumpPower * 0.5));
        setMomentumPreservingHorizontalVelocity(
                player,
                context,
                desiredHorizontal,
                jumpPower * 1.5,
                jumpPower
        );
    }

    /**
     * 使用向量总长度限制水平加速。冷却中可以转向但不降速；非冷却时只获得软上限允许的增量。
     */
    private static void setMomentumPreservingHorizontalVelocity(
            Player player,
            PlayerMovementContext context,
            Vec3 desiredHorizontal,
            double ySpeed,
            float jumpPower
    ) {
        Vec3 current = player.getDeltaMovement();
        Vec3 currentHorizontal = new Vec3(current.x, 0, current.z);
        double currentSpeed = currentHorizontal.horizontalDistance();
        double desiredSpeed = desiredHorizontal.horizontalDistance();

        Vec3 direction;
        if (desiredSpeed > HORIZONTAL_EPSILON) {
            direction = desiredHorizontal.scale(1.0 / desiredSpeed);
        } else if (currentSpeed > HORIZONTAL_EPSILON) {
            direction = currentHorizontal.scale(1.0 / currentSpeed);
        } else {
            direction = Vec3.ZERO;
        }

        // 墙跳拥有独立于普通跳跃的速度余量；超过上限时只保速，不强制减速。
        double wallKickSpeedLimit = context.getJumpLimitSpeed() + jumpPower * 0.5;
        double acceleration = context.getWallKickCooldown() == 0
                ? Math.min(
                        Math.max(0, desiredSpeed - currentSpeed),
                        Math.max(0, wallKickSpeedLimit - currentSpeed)
                )
                : 0;
        double targetSpeed = currentSpeed + acceleration;
        player.setDeltaMovement(direction.x * targetSpeed, ySpeed, direction.z * targetSpeed);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        super.clientTickRemote(player, context);
        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        ).normalize();
        Vec3 motionDirection = context.getSpeed().normalize();
        boolean isBackwardJump = (lookVec.x * motionDirection.x + lookVec.z * motionDirection.z) < 0;
        if (context.isLeftFootJump()) {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_LEFT : BaseState.JUMP_LEFT,
                    context);
        } else {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_RIGHT : BaseState.JUMP_RIGHT,
                    context);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.setWallKickCooldown(ServerConfig.WALL_KICK_ACCELERATION_COOLDOWN.getAsInt());
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
