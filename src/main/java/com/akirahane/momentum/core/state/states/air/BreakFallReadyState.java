package com.akirahane.momentum.core.state.states.air;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.context.PlayerMovementContext.AIR_LIMIT_ACCELERATION;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.AirborneState.FALL;
import static com.akirahane.momentum.core.state.states.air.AirborneState.canAirborne;
import static com.akirahane.momentum.core.state.states.ground.SlideState.*;
import static com.akirahane.momentum.core.state.states.ground.WalkState.canWalk;
import static com.akirahane.momentum.core.state.states.special.BreakFallState.canBreakFall;

public class BreakFallReadyState extends BaseState {
    // 动画名称
    public static String BREAK_FALL_READY = "break_fall_ready";

    public static boolean canBreakFallReady(Player player, PlayerMovementContext context) {
        return !player.onGround() && LOWER_CENTER.get().isDown();
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
        if (canAirborne(player, context) && !LOWER_CENTER.get().isDown()) {
            return StateType.AIRBORNE.getState();
        }
        if (canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.BREAK_FALL_READY.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setBreakFallReadyCount(6);
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        context.setJumpCooldown(15);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        clientTickRemote(player, context);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        if (player.fallDistance > player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) {
            float t = (float) ((player.fallDistance - 3.0f) / (70.0f - 3.0f));
            float speed = Math.clamp(t, 0.0f, 1.0f) * 1.5F + 0.5F;
            playStateAnimation(player, FALL, context, 20, speed);
        } else {
            playStateAnimation(player, IDLE, context);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.setBreakFallReadyCount(-1);
        context.setToBreakFallState(false);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL_READY;
    }
}
