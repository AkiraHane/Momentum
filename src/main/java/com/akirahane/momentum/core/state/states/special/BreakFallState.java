package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class BreakFallState extends BaseState {
    public static boolean canBreakFall(LocalPlayer player, PlayerMovementContext context) {
        return player.onGround() &&
                !player.isInLiquid() &&
                context.isLowerCenter() &&
                context.getLastFallDistance() > 2;
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
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (context.getBreakFallTimer() > 0) {
            return StateType.BREAK_FALL.getState();
        }
        if (WalkState.canWalk(player, context)){
            return StateType.WALK.getState();
        }
        return StateType.BREAK_FALL.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL;
    }
}
