package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.AirborneState.canAirborne;
import static com.akirahane.momentum.core.state.states.ground.WalkState.canWalk;

public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        return (LOWER_CENTER.get().isDown() || player.isSprinting() && player.isUnderWater()) &&
                player.isInWater() &&
                Minecraft.getInstance().options.keyUp.isDown();
    }

    public void onEnter(Player player, PlayerMovementContext context) {
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.debug("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMovementContext context) {
        player.setSwimming(false);
        player.setSprinting(false);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (player.isSwimming()) {
            return StateType.SWIM.getState();
        }
        if (canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.SWIM.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
