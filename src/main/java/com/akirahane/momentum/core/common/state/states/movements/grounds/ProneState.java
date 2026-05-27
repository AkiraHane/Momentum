package com.akirahane.momentum.core.common.state.states.movements.grounds;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.movements.GroundState;
import com.akirahane.momentum.core.common.context.PlayerMovementContext;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;


public class ProneState extends GroundState {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return StateType.PRONE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
    }
}
