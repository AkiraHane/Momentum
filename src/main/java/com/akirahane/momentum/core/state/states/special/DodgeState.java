package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import net.minecraft.world.entity.player.Player;

public class DodgeState extends BaseState {

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        return super.evaluate(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.DODGE;
    }
}
