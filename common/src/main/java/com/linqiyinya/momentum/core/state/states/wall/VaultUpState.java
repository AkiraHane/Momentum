package com.linqiyinya.momentum.core.state.states.wall;

import com.linqiyinya.momentum.client.config.ClientConfig;
import com.linqiyinya.momentum.client.hud.HintManager;
import com.linqiyinya.momentum.client.hud.WallHangHints;
import com.linqiyinya.momentum.config.ServerConfig;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.core.utils.WallInteractionUtils;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.world.entity.player.Player;

import static com.linqiyinya.momentum.core.context.PlayerMomentumContext.JUMP;

public class VaultUpState extends BaseState {

    public static boolean canVaultUp(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_VAULT_UP.getAsBoolean() && ClientConfig.ENABLE_VAULT_UP.getAsBoolean() &&
                WallInteractionUtils.selectWall(context, StateType.VAULT_UP, 0, wall -> wall.hasLedge()) &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMomentumContext context) {
        HintManager.add(WallHangHints.VAULT_UP);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    public void onEnter(Player player, PlayerMomentumContext context) {
        super.onEnter(player, context);
        context.setVaultTimer(6);
        player.setDeltaMovement(
                0,
                0.6,
                0
        );
    }

    @Override
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        Services.PLATFORM.setForcedPose(player, null);
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
