package com.akirahane.momentum.core.state.states;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class OriginalState extends BaseState {
    @Override
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.AIRBORNE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.resetEffect();
    }

    @Override
    public StateType getStateType() {
        return StateType.ORIGINAL;
    }
}
