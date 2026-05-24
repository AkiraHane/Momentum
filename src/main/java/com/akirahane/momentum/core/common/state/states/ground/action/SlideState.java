package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;


public class SlideState extends GroundState {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (!context.isLowerCenter()) {
            return WalkState.checkChildTransition(player, context);
        }
        if (player.getDeltaMovement().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get()) {
            return ProneState.checkChildTransition(player, context);
        }
        return StateType.SLIDE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
    }
}
