package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import net.minecraft.client.player.LocalPlayer;

public class WallClimbState extends BaseState {
    public static boolean canWallClimb(LocalPlayer player, PlayerMovementContext context) {
        return false;

    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_CLIMB;
    }
}
