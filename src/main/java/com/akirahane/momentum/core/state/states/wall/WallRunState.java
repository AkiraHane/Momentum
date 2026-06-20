package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.STEP;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.wall.WallHangState.canWallHang;

public class WallRunState extends BaseState {
    // 动画名称
    public static String WALL_RUN_LEFT = "wall_run_left";
    public static String WALL_RUN_RIGHT = "wall_run_right";

    private static final int SOUND_TICK = 10;

    public static boolean canWallRun(Player player, PlayerMovementContext context) {
        return !Vec3.ZERO.equals(context.getWallNormal()) &&
                !Vec3.ZERO.equals(context.getInputVec()) &&
                Mth.abs(context.getInputWallAngle()) > 45 && Mth.abs(context.getInputWallAngle()) < 90 &&
                canWallRunSpeedCheck(player, context) &&
                checkKey(player, context);
    }

    public static boolean canWallRunSpeedCheck(Player player, PlayerMovementContext context) {
        return context.getSpeed().horizontalDistance() * 20 > ServerConfig.MIN_WALL_RUN_SPEED.get() &&
                context.getSpeed().horizontalDistance() > Mth.abs((float) context.getSpeed().y);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_RUN);
        return Minecraft.getInstance().options.keyUp.isDown() &&
                Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        Vec3 wallNormal = context.getWallNormal();
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
        if (instance != null && instance.getModifier(WALL_GRAVITY_ID) != null) {
            if (context.isHasLedge()) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(-1F);
                player.setDeltaMovement(
                        tangent.x * context.getSpeed().horizontalDistance(),
                        0,
                        tangent.z * context.getSpeed().horizontalDistance()
                );
            } else {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -0.6,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
                context.setGravityModify(-0.6F);
                player.setDeltaMovement(
                        tangent.x * context.getSpeed().horizontalDistance(),
                        player.getDeltaMovement().y,
                        tangent.z * context.getSpeed().horizontalDistance()
                );
            }
        }
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            if (context.isHasLedge() && context.getGravityModify() != -1) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -1,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));

                player.setDeltaMovement(
                        player.getDeltaMovement().x,
                        0,
                        player.getDeltaMovement().z
                );
            } else if (context.getGravityModify() != -0.6) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        WALL_GRAVITY_ID,
                        -0.6,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
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
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        context.setNoMoveInput(false);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallKickState.canWallKick(player, context)) {
            return StateType.WALL_KICK.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if ((
                Vec3.ZERO.equals(context.getWallNormal()) ||
                        !Minecraft.getInstance().options.keyUp.isDown() ||
                        !canWallRunSpeedCheck(player, context)
        ) && AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_RUN.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_RUN;
    }
}
