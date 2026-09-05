package com.akirahane.momentum.network;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Client-only NeoForge transport adapter. */
public final class NeoForgeClientNetwork {
    private NeoForgeClientNetwork() {
    }

    public static void sendToggle() {
        ClientPacketDistributor.sendToServer(ToggleMomentumPacket.INSTANCE);
    }

    public static void sendStateTransition(StateTransitionPacket packet) {
        ClientPacketDistributor.sendToServer(packet);
    }
}
