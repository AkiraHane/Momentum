package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;


public class ProneState extends GroundState {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (!context.isLowerCenter()) {
            return WalkState.checkChildTransition(player, context);
        }
        return StateType.PRONE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
    }
}
