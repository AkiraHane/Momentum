package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;

public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        return ((LOWER_CENTER.get().isDown() || player.isSprinting()) && player.isUnderWater()) &&
                Minecraft.getInstance().options.keyUp.isDown();
    }

    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.trace("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        player.setSwimming(false);
        player.setSprinting(false);
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
