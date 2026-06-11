package com.akirahane.momentum.core.state.states.air;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.AirborneState.canAirborne;
import static com.akirahane.momentum.core.state.states.ground.SlideState.canSlide;
import static com.akirahane.momentum.core.state.states.ground.SlideState.canSlideSpeedCheck;
import static com.akirahane.momentum.core.state.states.ground.WalkState.canWalk;
import static com.akirahane.momentum.core.state.states.special.BreakFallState.canBreakFall;
import static com.akirahane.momentum.core.state.states.special.DodgeState.canDodge;
import static com.akirahane.momentum.core.state.states.water.SwimState.canSwim;

public class BreakFallReadyState extends BaseState {
    // 动画名称
    protected String BREAK_FALL_READY_SLIDE = "break_fall_ready_slide";

    protected String BREAK_FALL_READY = "break_fall_ready";

    public static boolean canBreakFallReady(Player player, PlayerMovementContext context) {
        return !player.onGround() && context.isLowerCenter();
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (canAirborne(player, context) && !context.isLowerCenter()) {
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
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        if (canSlideSpeedCheck(player, context)) {
            playStateAnimation(player, BREAK_FALL_READY_SLIDE, context);
        } else {
            playStateAnimation(player, BREAK_FALL_READY, context);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.setBreakFallReadyCount(-1);
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL_READY;
    }
}
