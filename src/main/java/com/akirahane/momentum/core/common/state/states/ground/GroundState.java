package com.akirahane.momentum.core.common.state.states.ground;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.states.ground.action.WalkState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public abstract class GroundState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return WalkState.checkChildTransition(player, context);
    }
}
