package com.akirahane.momentum.core.common.state.states;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.core.init.InitAttachments;
import net.minecraft.world.entity.player.Player;

public class OriginalState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (player.getData(InitAttachments.MOMENTUM_ENABLED)) {
            return MovementState.checkChildTransition(player, context);
        }
        return StateType.ORIGINAL.getState();
    }
}
