package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.init.InitSounds.JET2;
import static com.akirahane.momentum.core.context.PlayerMovementContext.STEP;

public class WallRunState extends BaseState {
    // 动画名称
    public static String WALL_RUN_LEFT = "wall_run_left";
    public static String WALL_RUN_RIGHT = "wall_run_right";

    private static final int SOUND_TICK = 10;

    public static boolean canWallRun(Player player, PlayerMovementContext context) {
        return !Vec3.ZERO.equals(context.getWallNormal()) &&
                !Vec3.ZERO.equals(context.getInputVec()) &&
                isLookAndSpeedSameSide(player, context) &&
                Mth.abs(context.getLookWallAngle()) >= 30 &&
                Mth.abs(context.getInputWallAngle()) >= 30 && Mth.abs(context.getInputWallAngle()) < 100 &&
                (context.isHasJetBooster() || canWallRunSpeedCheck(player, context)) &&
                checkKey(player, context) &&
                !player.onGround();
    }

    // 维持
    public static boolean canWallRunHold(Player player, PlayerMovementContext context) {
        return !Vec3.ZERO.equals(context.getWallNormal()) &&
                context.getWallNormal().equals(context.getRunWallNormal()) &&
                isLookAndSpeedSameSide(player, context) &&
                Mth.abs(context.getLookWallAngle()) >= 30 &&
                (context.isHasJetBooster() || canWallRunSpeedCheck(player, context)) &&
                checkKeyHold(player, context) &&
                !player.onGround();
    }
    public static boolean isLookAndSpeedSameSide(Player player, PlayerMovementContext context) {
        Vec3 wallNormal = context.getWallNormal();
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

        // 同号即同一侧
        return speedDot * lookDot > 0;
    }


    public static boolean canWallRunSpeedCheck(Player player, PlayerMovementContext context) {
        return context.getSpeed().horizontalDistance() * 20 > ServerConfig.MIN_WALL_RUN_SPEED.get() &&
                context.getSpeed().horizontalDistance() > (float) -context.getSpeed().y;
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_RUN);
        return Minecraft.getInstance().options.keyUp.isDown() &&
                Minecraft.getInstance().options.keyJump.isDown();
    }

    public static boolean checkKeyHold(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_RUN_HOLD);
        return Minecraft.getInstance().options.keyUp.isDown();
    }

    // 状态转换检查
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (OriginalState.canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (VaultInState.canVaultIn(player, context)) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallKickState.canWallKickRun(player, context)) {
            return StateType.WALL_KICK.getState();
        }
        if (WallRunState.canWallRunHold(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (VaultUpState.canVaultUp(player, context)) {
            return StateType.VAULT_UP.getState();
        }
        if (WallHangState.canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (BreakFallReadyState.canBreakFallReady(player, context)) {
            return StateType.BREAK_FALL_READY.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        LOGGER.warn("WallRunState evaluate error! 有状态没有覆盖!");
        return super.evaluate(player, context);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        Vec3 wallNormal = context.getWallNormal();
        context.setRunWallNormal(wallNormal);
        float inputWallAngle = context.getInputWallAngle();
        Vec3 currentMovement = player.getDeltaMovement();
        context.setNoMoveInput(true);
        Vec3 tangent = new Vec3(-wallNormal.z, 0, wallNormal.x);
        double dot = currentMovement.x * tangent.x + currentMovement.z * tangent.z;
        if (dot < 0) {
            tangent = tangent.scale(-1);
        }
        playStateAnimation(player, inputWallAngle > 0 ? WALL_RUN_LEFT : WALL_RUN_RIGHT, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            if (context.isHasLedge()) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(-1F);
                player.setDeltaMovement(
                        tangent.x * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()),
                        0,
                        tangent.z * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed())
                );
            } else {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -0.6,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(-0.6F);
                double ySpeed = player.getDeltaMovement().y;
                if (context.isHasJetBooster()) {
                    ySpeed = Math.max(0.62, ySpeed);
                    player.playSound(JET2.value(), 1F, 1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F);
                }
                player.setDeltaMovement(
                        tangent.x * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()),
                        ySpeed,
                        tangent.z * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed())
                );
            }
        }
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
        context.setTargetCameraRoll(inputWallAngle > 0 ? -15F : 15F);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        Vec3 wallNormal = context.getWallNormal();
        float inputWallAngle = context.getInputWallAngle();
        Vec3 currentMovement = player.getDeltaMovement();
        context.setNoMoveInput(true);
        Vec3 tangent = new Vec3(-wallNormal.z, 0, wallNormal.x);
        double dot = currentMovement.x * tangent.x + currentMovement.z * tangent.z;
        if (dot < 0) {
            tangent = tangent.scale(-1);
        }
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            double pushStrength = 0.05;
            if ((context.isHasLedge() || context.isHasJetBooster() && player.getDeltaMovement().y <= 0) && context.getGravityModify() != -1) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));

                player.setDeltaMovement(
                        tangent.x * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()) + wallNormal.x * pushStrength,
                        0,
                        tangent.z * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()) + wallNormal.z * pushStrength
                );
            } else if (context.getGravityModify() != -0.6) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -0.6,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                player.setDeltaMovement(
                        tangent.x * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()) + wallNormal.x * pushStrength,
                        player.getDeltaMovement().y,
                        tangent.z * Math.max(player.getDeltaMovement().horizontalDistance(), context.getJumpLimitSpeed()) + wallNormal.z * pushStrength
                );
            }
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        float speed = (float) Math.min(context.getSpeed().length() * 5, 5);
        playStateAnimation(player, context.getCurrentAnimationName(), context, 0, speed);
        context.setNeedSoundTick(context.getNeedSoundTick() - speed);
        if (context.getNeedSoundTick() <= 0) {
            context.playWallSound(player, STEP, 0.15F, 1);
            context.setNeedSoundTick(context.getNeedSoundTick() + SOUND_TICK);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
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
}
