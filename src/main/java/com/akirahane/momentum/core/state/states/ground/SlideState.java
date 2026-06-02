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

import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.LOCAL_VALUE;
import static com.akirahane.momentum.core.effect.MomentumEffectType.BLOCK_FRICTION;


public class SlideState extends BaseState {
    // 动画名称
    protected String SLIDE = "slide";

    // 跳跃减速窗口时间
    protected int JUMP_DECELERATION_WINDOW = 5;

    public static boolean canSlide(Player player, PlayerMovementContext context) {
        return player.onGround() &&
                context.isLowerCenter() &&
                player.isSprinting() &&
                context.getSpeed().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get() &&
                context.getOldSpeed().horizontalDistance() > -context.getOldSpeed().y;
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).add(context.SLIDE_FRICTION);
        context.SLIDE_BLOCK_FRICTION.setDuration(JUMP_DECELERATION_WINDOW);
        context.getPendingEffectPool().get(BLOCK_FRICTION).add(
                context.SLIDE_BLOCK_FRICTION
        );

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
        playStateAnimation(player, SLIDE, context, 6);
    }

    public void onExit(Player player, PlayerMovementContext context) {
        if (JUMP_DECELERATION_WINDOW >= (ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get() - context.getSlideCooldown())) {
            context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(context.SLIDE_FRICTION);
            context.getPendingEffectPool().get(BLOCK_FRICTION).remove(context.SLIDE_BLOCK_FRICTION);
            context.getPendingEffectPool().get(MomentumEffectType.FRICTION).add(
                    new MomentumEffect(
                            new Vec3(0.1, 0, 0),
                            Vec3.ZERO,
                            LOCAL_VALUE,
                            5
                    )
            );
        }
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(context.SLIDE_FRICTION);
        context.getPendingEffectPool().get(MomentumEffectType.BLOCK_FRICTION)
                .remove(context.SLIDE_BLOCK_FRICTION);
        player.setSprinting(false);
        context.setSlopeUnitVector(Vec3.ZERO);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (!context.isLowerCenter()) {
            return StateType.WALK.getState();
        }
        if (context.getSpeed().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get()) {
            return StateType.PRONE.getState();
        }
        return StateType.SLIDE.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.SLIDE;
    }
}
