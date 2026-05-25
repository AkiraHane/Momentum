package com.akirahane.momentum.core.common.state.states.air;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public class AirborneState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return StateType.AIR.getState();
    }
}
