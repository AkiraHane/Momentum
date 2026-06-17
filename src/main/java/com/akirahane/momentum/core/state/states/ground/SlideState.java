package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import com.akirahane.momentum.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.LOCAL_VALUE;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.BreakFallReadyState.canBreakFallReady;


public class SlideState extends BaseState {
    // 动画名称
    public static String SLIDE = "slide";

    // 跳跃减速窗口时间
    protected int JUMP_DECELERATION_WINDOW = 5;

    public static boolean canSlide(Player player, PlayerMovementContext context) {
        return player.onGround() &&
                context.isLowerCenter() &&
                player.isSprinting() &&
                canSlideSpeedCheck(player, context);
    }

    public static boolean canSlideSpeedCheck(Player player, PlayerMovementContext context) {
        return context.getSpeed().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get() &&
                context.getOldSpeed().horizontalDistance() >= -context.getOldSpeed().y;
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.addPermanentEffect(MomentumEffectType.FRICTION, context.SLIDE_FRICTION);
        context.addEffect(MomentumEffectType.BLOCK_FRICTION, context.SLIDE_BLOCK_FRICTION, JUMP_DECELERATION_WINDOW);

        if (context.getSlideCooldown() == 0) {
            Vec3 velocity = player.getDeltaMovement();
            float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
            LOGGER.debug("player.getJumpPower() {}", jumpPower);
            player.addDeltaMovement(
                    new Vec3(
                            velocity.x * jumpPower / velocity.horizontalDistance(),
                            0,
                            velocity.z * jumpPower / velocity.horizontalDistance()
                    )
            );
        }
        context.setSlideCooldown(ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get());
        playStateAnimation(player, SLIDE, context, 6, 1.0f);
    }

    public void onExit(Player player, PlayerMovementContext context) {
        if (JUMP_DECELERATION_WINDOW >= (ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get() - context.getSlideCooldown())) {
            // 如果跳跃的时间小于冷却, 则增加移动方向的阻力
            context.addEffect(
                    MomentumEffectType.FRICTION,
                    new MomentumEffect(
                            new Vec3(0.1, 0, 0),
                            Vec3.ZERO,
                            LOCAL_VALUE,
                            5
                    ),
                    5
            );
        }
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.removeEffect(MomentumEffectType.FRICTION, context.SLIDE_FRICTION);
        context.removeEffect(MomentumEffectType.BLOCK_FRICTION, context.SLIDE_BLOCK_FRICTION);
        player.setSprinting(false);
        context.setSlopeUnitVector(Vec3.ZERO);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (canBreakFallReady(player, context)) {
            return StateType.BREAK_FALL_READY.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        boolean canCrouching = canPlayerFitAtPose(player, Pose.CROUCHING);
        if (!context.isLowerCenter() && canCrouching) {
            return StateType.WALK.getState();
        }
        if (!context.isLowerCenter()){
            return StateType.PRONE.getState();
        }
        if (context.getSpeed().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get() / 2) {
            return StateType.PRONE.getState();
        }
        return StateType.SLIDE.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.SLIDE;
    }
}
