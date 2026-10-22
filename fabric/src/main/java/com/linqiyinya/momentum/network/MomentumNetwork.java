package com.linqiyinya.momentum.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class MomentumNetwork {

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ToggleMomentumPacket.TYPE, ToggleMomentumPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(StateTransitionPacket.TYPE, StateTransitionPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(StateBroadcastPacket.TYPE, StateBroadcastPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                ToggleMomentumPacket.TYPE,
                (packet, context) -> MomentumNetworkHelper.onToggleMomentum(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
                StateTransitionPacket.TYPE,
                (packet, context) -> MomentumNetworkHelper.onStateTransition(context.player(), packet)
        );
    }
}
