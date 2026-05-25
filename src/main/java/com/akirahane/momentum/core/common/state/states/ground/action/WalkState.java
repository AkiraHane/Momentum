package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

public class WalkState extends GroundState {
    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }

    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (context.isLowerCenter() && player.getDeltaMovement().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get()) {
            return ProneState.checkChildTransition(player, context);
        }
        if (context.isLowerCenter() && player.getDeltaMovement().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get()) {
            return SlideState.checkChildTransition(player, context);
        }
        return StateType.WALK.getState();
    }
}
