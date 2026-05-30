package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.context.PlayerMovementContext.JUMP;

public class VaultState extends BaseState {

    public static boolean canVault(LocalPlayer player, PlayerMovementContext context) {
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setVaultTimer(10);
    }

    @Override
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
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
