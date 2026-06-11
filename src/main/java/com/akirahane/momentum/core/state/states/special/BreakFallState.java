package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class BreakFallState extends BaseState {
    public static boolean canBreakFall(Player player, PlayerMovementContext context) {
        return context.isToBreakFallState() && context.getLastFallDistance() > 4;
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setBreakFallTimer(10);
        context.setNoJump(true);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.setNoJump(false);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (context.getBreakFallTimer() > 0) {
            return StateType.BREAK_FALL.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.BREAK_FALL.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL;
    }
}
