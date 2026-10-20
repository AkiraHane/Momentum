package com.akirahane.momentum.client.network;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.network.StateBroadcastPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientNetwork {

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(
                StateBroadcastPacket.TYPE,
                (packet, context) -> MomentumClientNetworkHelper.onStateBroadcast(packet)
        );
    }
}
