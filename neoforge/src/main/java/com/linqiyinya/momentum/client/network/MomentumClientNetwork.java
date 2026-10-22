package com.linqiyinya.momentum.client.network;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.network.StateBroadcastPacket;
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
