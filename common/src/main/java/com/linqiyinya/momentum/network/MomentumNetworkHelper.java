package com.linqiyinya.momentum.network;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.core.utils.WallInteractionUtils;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MomentumNetworkHelper {

    public static StateBroadcastPacket stateSnapshot(Player player) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        return new StateBroadcastPacket(
                player.getId(),
                stateMachine.getCurrentState().getStateType(),
                stateMachine.getContext().getTransitionExtraData(),
                WallInteractionUtils.wallData(stateMachine.getContext())
        );
    }

    public static void onToggleMomentum(Player player) {
        boolean enabled = !Services.PLATFORM.isMomentumEnabled(player);
        Services.PLATFORM.setMomentumEnabled(player, enabled);
        Constants.LOG.debug("[DATA] TOGGLE side=server player={} dimension={} enabled={}",
                player.getId(), player.level().dimension().identifier(), enabled);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(
                    Component.translatable(enabled
                            ? "message.momentum.momentum_enabled"
                            : "message.momentum.momentum_disabled"),
                    true
            );
        }
    }

    public static void onStateTransition(Player player, StateTransitionPacket packet) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        Constants.LOG.debug("[NET] C2S_RECEIVE side=server player={} dimension={} state={} extra={} wall={}",
                player.getId(), player.level().dimension().identifier(), packet.stateType(), packet.extraData(), packet.wallData());
        // onEnter() 需要客户端传入的动作和墙面数据。
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), player);
    }
}
