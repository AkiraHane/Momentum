package com.akirahane.momentum.init;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Momentum.MODID)
public class ModNetwork {

    @SubscribeEvent // on the mod event bus
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                ToggleMomentumPacket.TYPE,
                ToggleMomentumPacket.STREAM_CODEC,
                (packet, context) -> {
                    // 已经在主线程了，NeoForge 1.21.1默认在主线程处理
                    Player player = context.player();
                    boolean current = player.getData(ModAttachments.MOMENTUM_ENABLED);
                    player.setData(ModAttachments.MOMENTUM_ENABLED, !current);

                    player.sendOverlayMessage(
                            Component.translatable(current
                                    ? "message.momentum.momentum_disabled"
                                    : "message.momentum.momentum_enabled")
                    );
                }
        );
    }
}
