package com.akirahane.momentum.core.common.state.states.movements.grounds;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.base.BaseState;
import com.akirahane.momentum.core.common.state.states.movements.GroundState;
import com.akirahane.momentum.core.common.context.PlayerMovementContext;
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
