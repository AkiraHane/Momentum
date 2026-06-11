package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.context.PlayerMovementContext.JUMP;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class VaultState extends BaseState {

    public static boolean canVault(Player player, PlayerMovementContext context) {
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setVaultTimer(10);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (context.getVaultTimer() <= 0) {
            return StateType.WALL_CLIMB.getState();
        }
        return StateType.VAULT.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT;
    }
}
