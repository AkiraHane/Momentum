package com.akirahane.momentum.network;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.platform.Services;
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
                stateMachine.getContext().getTransitionWallData()
        );
    }

    public static void onToggleMomentum(Player player) {
        boolean enabled = !Services.PLATFORM.isMomentumEnabled(player);
        Services.PLATFORM.setMomentumEnabled(player, enabled);
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
        // onEnter() 需要客户端传入的动作和墙面数据。
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), player);
    }
}
