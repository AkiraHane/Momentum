package com.linqiyinya.momentum.core.state.states.wall;

import com.linqiyinya.momentum.client.config.ClientConfig;
import com.linqiyinya.momentum.client.hud.HintManager;
import com.linqiyinya.momentum.client.hud.WallHangHints;
import com.linqiyinya.momentum.config.ServerConfig;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.mixin.LivingEntityAccessor;
import com.linqiyinya.momentum.core.utils.WallSoundUtils;
import com.linqiyinya.momentum.core.utils.WallInteractionUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.linqiyinya.momentum.core.context.PlayerMomentumContext.*;

public class WallKickState extends BaseState {

    private static final int WALL_KICK_STATE_TICKS = 5;
    private static final int WALL_RUN_REENTRY_GRACE_TICKS = 8;
    private static final double HORIZONTAL_EPSILON = 1.0E-6;
    private static final double WALL_KICK_UPWARD_BIAS_DEGREES = 30;
    private static final double WALL_KICK_PITCH_LIMIT_DEGREES = 60;
    private static final double WALL_KICK_FULL_VERTICAL_ANGLE_DEGREES = 45;
    private static final double WALL_KICK_MAX_VERTICAL_MULTIPLIER = 1.5;

    public static boolean canWallKick(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                !player.onGround() &&
                context.getInputVec().horizontalDistance() > 0.01 &&
                WallInteractionUtils.selectWall(context, StateType.WALL_KICK, 180,
                        wall -> Mth.abs(wall.inputAngle()) >= 120) &&
                checkKey(player, context);
    }

    // 跑墙特殊进入条件
    public static boolean canWallKickRun(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                WallInteractionUtils.selectWall(context, StateType.WALL_KICK, 180, wall -> true) &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMomentumContext context) {
        HintManager.add(WallHangHints.WALL_KICK);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        return withPredicate(DEFAULT_CHAIN, StateType.WALL_KICK, (p, c) -> c.getWallJumpTimer() > 0);
    }

    @Override
    public void onEnter(Player player, PlayerMomentumContext context) {
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
        applyInputDirectedKick(player, context, jumpPower);
        if (fromWallRun) {
            context.setWallRunReentryGraceTicks(WALL_RUN_REENTRY_GRACE_TICKS);
        }
        if (Mth.abs(context.getInputWallAngle()) >= 120) {
            player.fallDistance = 0;
        }
        WallSoundUtils.playWallSound(player, context, WallSoundUtils.FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
        context.setWallJumpTimer(WALL_KICK_STATE_TICKS);
    }

    /** 直接墙跳与跑墙墙跳共用输入方向、俯仰和角度衰减。 */
    private static void applyInputDirectedKick(Player player, PlayerMomentumContext context, float jumpPower) {
        double angle = Mth.abs(context.getInputWallAngle());
        // 360 表示没有有效墙面或输入方向。
        if (angle <= 90 || angle > 180) return;

        Vec3 input = new Vec3(context.getInputVec().x, 0, context.getInputVec().z);
        double inputLength = input.horizontalDistance();
        if (inputLength <= HORIZONTAL_EPSILON) return;

        // 90° 无力度，180° 满力度；120° 约有 70% 的力度。
        double progress = (angle - 90) / 90;
        double remaining = 1 - progress;
        double angleFactor = 1 - remaining * remaining * remaining;
        double strength = jumpPower * angleFactor;

        double yaw = Math.toRadians(player.getYRot());
        double alignment = Mth.clamp(
                (-input.x * Math.sin(yaw) + input.z * Math.cos(yaw)) / inputLength,
                -1.0,
                1.0
        );
        // 前进保留俯仰，侧移归零，后退反转；限幅后统一向上补正。
        double weightedPitch = Mth.clamp(
                player.getXRot() * alignment,
                -WALL_KICK_PITCH_LIMIT_DEGREES,
                WALL_KICK_PITCH_LIMIT_DEGREES
        );
        double pitchDegrees = weightedPitch - WALL_KICK_UPWARD_BIAS_DEGREES;
        double pitch = Math.toRadians(pitchDegrees);

        Vec3 current = player.getDeltaMovement();
        Vec3 currentHorizontal = new Vec3(current.x, 0, current.z);
        double horizontalFactor = (1 + Math.cos(pitch)) * 0.5;
        Vec3 horizontalImpulse = input.scale(horizontalFactor * strength / inputLength);
        Vec3 desiredHorizontal = currentHorizontal.add(horizontalImpulse);

        // 最终向上 45° 达到目标 Y 速度上限；墙面力度越弱，越接近原 Y 速度。
        double verticalFactor = Mth.clamp(-pitchDegrees / WALL_KICK_FULL_VERTICAL_ANGLE_DEGREES, -1.0, 1.0);
        double targetYSpeed = jumpPower * WALL_KICK_MAX_VERTICAL_MULTIPLIER * verticalFactor;
        double ySpeed = Mth.lerp(angleFactor, current.y, targetYSpeed);
        setMomentumPreservingHorizontalVelocity(
                player,
                context,
                desiredHorizontal,
                ySpeed,
                jumpPower
        );
    }

    /** 使用向量总长度限制水平加速，冷却期间线性恢复加速收益。 */
    private static void setMomentumPreservingHorizontalVelocity(
            Player player,
            PlayerMomentumContext context,
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
        int cooldownTicks = ServerConfig.WALL_KICK_ACCELERATION_COOLDOWN.getAsInt();
        double recoveryFactor = cooldownTicks > 0
                ? Mth.clamp(1.0 - (double) context.getWallKickCooldown() / cooldownTicks, 0.0, 1.0)
                : 1.0;
        double acceleration = Math.min(
                Math.max(0, desiredSpeed - currentSpeed),
                Math.max(0, wallKickSpeedLimit - currentSpeed)
        ) * recoveryFactor;
        double targetSpeed = currentSpeed + acceleration;
        player.setDeltaMovement(direction.x * targetSpeed, ySpeed, direction.z * targetSpeed);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMomentumContext context) {
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
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        context.setWallKickCooldown(ServerConfig.WALL_KICK_ACCELERATION_COOLDOWN.getAsInt());
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
