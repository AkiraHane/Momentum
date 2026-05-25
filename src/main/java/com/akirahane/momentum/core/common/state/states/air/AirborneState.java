package com.akirahane.momentum.core.common.state.states.air;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import net.minecraft.world.entity.player.Player;

public class AirborneState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return StateType.AIR.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION_LIMIT_SPEED).setDuration(-1);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION_LIMIT_SPEED).setValue(0.6F);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION).setDuration(-1);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION).setModifyValue(0.1F);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION_LIMIT_SPEED).init();
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_ACCELERATION).init();
    }
}
