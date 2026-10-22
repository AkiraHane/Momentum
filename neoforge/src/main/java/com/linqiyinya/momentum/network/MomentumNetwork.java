package com.linqiyinya.momentum.network;

import com.linqiyinya.momentum.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class MomentumNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ToggleMomentumPacket.TYPE,
                ToggleMomentumPacket.STREAM_CODEC,
                (packet, context) -> MomentumNetworkHelper.onToggleMomentum(context.player())
        );

        registrar.playToServer(
                StateTransitionPacket.TYPE,
                StateTransitionPacket.STREAM_CODEC,
                (packet, context) -> MomentumNetworkHelper.onStateTransition(context.player(), packet)
        );

        registrar.playToClient(
                StateBroadcastPacket.TYPE,
                StateBroadcastPacket.STREAM_CODEC
        );
    }
}
