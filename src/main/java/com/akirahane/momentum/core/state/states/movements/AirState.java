package com.akirahane.momentum.core.state.states.movements;

import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.MovementState;
import com.akirahane.momentum.core.state.states.movements.air.AirborneState;
import net.minecraft.world.entity.player.Player;

public abstract class AirState extends MovementState {

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return AirborneState.checkChildTransition(player, context, nowState);
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }
}
