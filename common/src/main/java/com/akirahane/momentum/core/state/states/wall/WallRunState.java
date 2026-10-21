package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.utils.WallInteractionUtils;
import com.akirahane.momentum.core.utils.WallSoundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.registry.MomentumSounds.JET2;
import static com.akirahane.momentum.core.utils.WallSoundUtils.STEP;

public class WallRunState extends BaseState {
    // 动画名称
    public static String WALL_RUN_LEFT = "wall_run_left";
    public static String WALL_RUN_RIGHT = "wall_run_right";

    private static final int SOUND_TICK = 10;
    private static final double WALL_PUSH_STRENGTH = 0.05;
    private static final double VERTICAL_SPEED_RETAINED_PER_TICK = 0.8;
    private static final double VERTICAL_SPEED_EPSILON = 0.01;

    public static boolean canWallRun(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_WALL_RUN.getAsBoolean() && ClientConfig.ENABLE_WALL_RUN.getAsBoolean() &&
                !Vec3.ZERO.equals(context.getInputVec()) &&
                WallInteractionUtils.selectWall(context, StateType.WALL_RUN, 0,
                        wall -> wall.hasFaceWall() && isLookAndSpeedSameSide(player, wall.normal()) &&
                                Mth.abs(wall.lookAngle()) >= 30 &&
                                Mth.abs(wall.inputAngle()) >= 30 && Mth.abs(wall.inputAngle()) < 100) &&
                canWallRunSpeedCheck(player, context) &&
                checkKey(player, context) &&
                !player.onGround();
    }

    // 维持
    public static boolean canWallRunHold(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_WALL_RUN.getAsBoolean() && ClientConfig.ENABLE_WALL_RUN.getAsBoolean() &&
                WallInteractionUtils.selectWall(context, StateType.WALL_RUN, 0,
                        wall -> isLookAndSpeedSameSide(player, wall.normal()) && Mth.abs(wall.lookAngle()) >= 30) &&
                canWallRunSpeedCheck(player, context) &&
                checkKeyHold(player, context) &&
                !player.onGround();
    }
    public static boolean isLookAndSpeedSameSide(Player player, Vec3 wallNormal) {
        if (Vec3.ZERO.equals(wallNormal)) return false;

        Vec3 speed = player.getDeltaMovement();
        // 水平速度太小时认为没有方向，直接通过（避免误判）
        if (speed.horizontalDistanceSqr() < 1.0E-4) return true;

        // 视角水平方向
        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        );

        // 墙面切线（水平方向，与法线垂直）
        Vec3 tangent = new Vec3(-wallNormal.z, 0, wallNormal.x);

        double speedDot = speed.x * tangent.x + speed.z * tangent.z;
        double lookDot = lookVec.x * tangent.x + lookVec.z * tangent.z;

        // 将速度切线分量归一化，与视角切线分量(已是单位向量)比较
        // 允许视角稍微往后看: cos(θ) > -0.3, 即夹角不超过约 107°
        double speedTangentNorm = speedDot / Math.max(speed.horizontalDistance(), 0.001);
        return speedTangentNorm * lookDot > -0.4;
    }


    public static boolean canWallRunSpeedCheck(Player player, PlayerMomentumContext context) {
        boolean hasReentryGrace = context.getWallRunReentryGraceTicks() > 0;
        double horizontalSpeed = context.getSpeed().horizontalDistance();
        if (hasReentryGrace) {
            // 碰到下一面墙时，单 tick 实际位移可能因碰撞偏小；宽限期内同时参考当前动量。
            horizontalSpeed = Math.max(horizontalSpeed, player.getDeltaMovement().horizontalDistance());
        }
        double minimumSpeed = ServerConfig.MIN_WALL_RUN_SPEED.get() * (hasReentryGrace ? 0.75 : 1.0);
        return horizontalSpeed * 20 > minimumSpeed &&
                (hasReentryGrace || context.isHasJetBooster() || horizontalSpeed > (float) -context.getSpeed().y);
    }

    public static boolean checkKey(Player player, PlayerMomentumContext context) {
        HintManager.add(WallHangHints.WALL_RUN);
        return Minecraft.getInstance().options.keyUp.isDown() &&
                Minecraft.getInstance().options.keyJump.isDown();
    }

    public static boolean checkKeyHold(Player player, PlayerMomentumContext context) {
        HintManager.add(WallHangHints.WALL_RUN_HOLD);
        return Minecraft.getInstance().options.keyUp.isDown();
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 墙跑中蹬墙跳更宽松（canWallKickRun）；自身进入检查换成保持检查
        return withPredicate(
                withPredicate(DEFAULT_CHAIN, StateType.WALL_KICK, WallKickState::canWallKickRun),
                StateType.WALL_RUN, WallRunState::canWallRunHold);
    }

    @Override
    public void onEnter(Player player, PlayerMomentumContext context) {
        Vec3 wallNormal = context.getWallNormal();
        float inputWallAngle = context.getInputWallAngle();
        Vec3 currentMovement = player.getDeltaMovement();
        context.setNoMoveInput(true);
        Vec3 tangent = new Vec3(-wallNormal.z, 0, wallNormal.x);
        double dot = currentMovement.x * tangent.x + currentMovement.z * tangent.z;
        if (dot < 0) {
            tangent = tangent.scale(-1);
        }
        boolean useLeft;
        if (player.level().isClientSide() && Minecraft.getInstance().player == player) {
            // 本地玩家编码左右方向，墙面由状态机统一同步。
            useLeft = inputWallAngle > 0;
            context.setTransitionExtraData(useLeft ? 1 : 0);
        } else {
            // 服务端/远程玩家: 从网络包恢复
            int extra = context.getTransitionExtraData();
            useLeft = extra >= 0 ? (extra == 1) : (inputWallAngle > 0);
        }
        playStateAnimation(player, useLeft ? WALL_RUN_LEFT : WALL_RUN_RIGHT, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        double ySpeed = currentMovement.y > 0 ? currentMovement.y : 0;
        if (context.isHasJetBooster()) {
            ySpeed = dampVerticalSpeed(currentMovement.y);
            player.playSound(JET2.get(), 1F, 1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F);
            player.fallDistance = 0;
        }
        double wallRunSpeed = Math.max(currentMovement.horizontalDistance(), context.getJumpLimitSpeed());
        if (instance != null) {
            if (context.isHasLedge()) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(-1F);
                player.setDeltaMovement(
                        tangent.x * wallRunSpeed + wallNormal.x * WALL_PUSH_STRENGTH,
                        0,
                        tangent.z * wallRunSpeed + wallNormal.z * WALL_PUSH_STRENGTH
                );
                player.fallDistance = 0;
            } else {
                float gravityModify = context.isHasJetBooster() ? -1F : -0.6F;
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        gravityModify,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(gravityModify);
                player.setDeltaMovement(
                        tangent.x * wallRunSpeed + wallNormal.x * WALL_PUSH_STRENGTH,
                        ySpeed,
                        tangent.z * wallRunSpeed + wallNormal.z * WALL_PUSH_STRENGTH
                );
            }
        }
        context.setNeedSoundTick(SOUND_TICK);
        WallSoundUtils.playWallSound(player, context, STEP, 0.15F, 1);
        context.setTargetCameraRoll(inputWallAngle > 0 ? -15F : 15F);
    }

    @Override
    public void clientTick(Player player, PlayerMomentumContext context) {
        super.clientTick(player, context);
        Vec3 wallNormal = context.getWallNormal();
        Vec3 currentMovement = player.getDeltaMovement();
        context.setNoMoveInput(true);
        Vec3 tangent = new Vec3(-wallNormal.z, 0, wallNormal.x);
        double dot = currentMovement.x * tangent.x + currentMovement.z * tangent.z;
        if (dot < 0) {
            tangent = tangent.scale(-1);
        }
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            boolean stopAtLedge = context.isHasLedge();
            boolean dampWithBooster = !stopAtLedge && context.isHasJetBooster();
            float gravityModify = stopAtLedge || dampWithBooster ? -1F : -0.6F;
            if (context.getGravityModify() != gravityModify) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        gravityModify,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(gravityModify);
            }

            // 只按切线速度维持墙跑，避免吸墙法线分量在每 tick 被重复计入而产生异常加速。
            double tangentSpeed = Math.abs(currentMovement.dot(tangent));
            double wallRunSpeed = Math.max(tangentSpeed, context.getJumpLimitSpeed());
            double ySpeed = stopAtLedge
                    ? 0
                    : dampWithBooster ? dampVerticalSpeed(currentMovement.y) : currentMovement.y;
            player.setDeltaMovement(
                    tangent.x * wallRunSpeed + wallNormal.x * WALL_PUSH_STRENGTH,
                    ySpeed,
                    tangent.z * wallRunSpeed + wallNormal.z * WALL_PUSH_STRENGTH
            );
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMomentumContext context) {
        float speed = (float) Math.min(context.getSpeed().length() * 5, 5);
        playStateAnimation(player, context.getCurrentAnimationName(), context, 0, speed);
        context.setNeedSoundTick(context.getNeedSoundTick() - speed);
        if (context.getNeedSoundTick() <= 0) {
            WallSoundUtils.playWallSound(player, context, STEP, 0.15F, 1);
            context.setNeedSoundTick(context.getNeedSoundTick() + SOUND_TICK);
        }
    }

    @Override
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        context.setTargetCameraRoll(0F);  // 退出时回正

        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        context.setNoMoveInput(false);
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_RUN;
    }

    private static double dampVerticalSpeed(double speed) {
        double dampedSpeed = speed * VERTICAL_SPEED_RETAINED_PER_TICK;
        return Math.abs(dampedSpeed) < VERTICAL_SPEED_EPSILON ? 0 : dampedSpeed;
    }

}
