package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public class WalkState extends BaseState {
    public static boolean canWalk(Player player, PlayerMovementContext context) {
        return player.onGround();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALK;
    }
}
