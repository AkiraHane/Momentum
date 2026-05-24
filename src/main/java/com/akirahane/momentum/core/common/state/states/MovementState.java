package com.akirahane.momentum.core.common.state.states;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public abstract class MovementState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (player.onGround()) {
            return GroundState.checkChildTransition(player, context);
        } else {
            return null;
        }

    }
}
