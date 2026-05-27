package com.akirahane.momentum.core.state.states.movements.water;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.WaterState;
import net.minecraft.world.entity.player.Player;

public class SwimState extends WaterState {

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.SWIM.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.debug("player.isSwimming(): {}", player.isSwimming());
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setSwimming(false);
        player.setSprinting(false);
    }
}
