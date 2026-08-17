package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.context.PlayerMovementContext.JUMP;

public class VaultUpState extends BaseState {

    public static boolean canVaultUp(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_VAULT_UP.getAsBoolean() && ClientConfig.ENABLE_VAULT_UP.getAsBoolean() &&
                context.isHasLedge() &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.VAULT_UP);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        context.setVaultTimer(6);
        player.setDeltaMovement(
                0,
                0.6,
                0
        );
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        player.setForcedPose(null);
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 上翻持续期自保持，且比默认入口更早（紧跟 SwimDash），避免翻越期间被打断；上翻时不再考虑翻入
        return moveAfter(
                without(DEFAULT_CHAIN, StateType.VAULT_IN),
                StateType.VAULT_UP, StateType.SWIM_DASH,
                (p, c) -> c.getVaultTimer() > 0);
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT_UP;
    }
}
