package com.linqiyinya.momentum.client.network;

import com.linqiyinya.momentum.network.StateBroadcastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MomentumClientNetwork implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                StateBroadcastPacket.TYPE,
                (packet, context) -> MomentumClientNetworkHelper.onStateBroadcast(packet)
        );
    }
}
