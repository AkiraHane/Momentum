package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WallClimbState extends BaseState {
    // 动画名称
    public static String WALL_CLIMB = "wall_climb";

    private static final int SOUND_TICK = 10;

    public static boolean canWallClimb(Player player, PlayerMovementContext context) {
        return player.onClimbable() || (
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                        context.isHasFaceWall() &&
                        Minecraft.getInstance().options.keyJump.isDown() &&
                        !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 45 &&
                        Mth.abs(context.getLookWallAngle()) < 45 &&
                        context.getSpeed().y > 0
        );

    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallHangState.canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (WallKickState.canWallKick(player, context)){
            return StateType.WALL_KICK.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_CLIMB.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.addPermanentEffect(MomentumEffectType.ACCELERATION, AIR_ACCELERATION);
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        playStateAnimation(player, WALL_CLIMB, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -0.8,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
        player.addDeltaMovement(new Vec3(0, player.getDeltaMovement().y * 0.2, 0));
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        float speed = (float) Math.min(context.getSpeed().y * 6, 5);
        playStateAnimation(player, WALL_CLIMB, context, 0, speed);
        context.setNeedSoundTick(context.getNeedSoundTick() - speed);
        if (context.getNeedSoundTick() <= 0){
            context.playWallSound(player, STEP, 0.15F, 1);
            context.setNeedSoundTick(context.getNeedSoundTick() + SOUND_TICK);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.removeEffect(MomentumEffectType.ACCELERATION, AIR_ACCELERATION);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_CLIMB;
    }
}
