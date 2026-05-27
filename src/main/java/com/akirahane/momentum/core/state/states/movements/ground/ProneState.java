package com.akirahane.momentum.core.state.states.movements.ground;

import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.GroundState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;


public class ProneState extends GroundState {
    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.PRONE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
    }
}
