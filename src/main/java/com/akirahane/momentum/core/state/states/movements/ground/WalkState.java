package com.akirahane.momentum.core.state.states.movements.ground;

import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.GroundState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public class WalkState extends GroundState {
    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.WALK.getState();
    }
}
