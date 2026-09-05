package com.akirahane.momentum.fabric.network;

import com.akirahane.momentum.fabric.config.FabricServerConfig;
import com.akirahane.momentum.network.MovementPacketHandlers;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;

/** Client-only Fabric transport adapter. */
public final class FabricClientNetwork {
    private FabricClientNetwork() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(FabricServerConfigPacket.TYPE,
                (packet, context) -> FabricServerConfig.applySynchronized(packet.json()));
        ClientPlayNetworking.registerGlobalReceiver(StateBroadcastPacket.TYPE, (packet, context) -> {
            LocalPlayer player = context.player();
            if (player != null) {
                MovementPacketHandlers.handleStateBroadcast(player, packet);
            }
        });
    }

    public static void sendToggle() {
        ClientPlayNetworking.send(ToggleMomentumPacket.INSTANCE);
    }

    public static void sendStateTransition(StateTransitionPacket packet) {
        ClientPlayNetworking.send(packet);
    }
}
