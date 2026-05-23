package com.akirahane.momentum.core.init;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.network.StateTransitionPacket;
import com.akirahane.momentum.core.network.SyncMomentumEnabledPacket;
import com.akirahane.momentum.core.network.ToggleMomentumPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
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
                    boolean current = !player.getData(ModAttachments.MOMENTUM_ENABLED);
                    player.setData(ModAttachments.MOMENTUM_ENABLED, current);

                    player.sendOverlayMessage(
                            Component.translatable(current
                                    ? "message.momentum.momentum_enabled"
                                    : "message.momentum.momentum_disabled")
                    );

                    // 同步给客户端
                    if (player instanceof ServerPlayer sp) {
                        PacketDistributor.sendToPlayer(sp, new SyncMomentumEnabledPacket(current));
                    }
                }
        );

        registrar.playToClient(
                SyncMomentumEnabledPacket.TYPE,
                SyncMomentumEnabledPacket.STREAM_CODEC,
                (packet, context) -> {
                    Player player = context.player();
                    player.setData(ModAttachments.MOMENTUM_ENABLED, packet.enabled());
                }
        );

        registrar.playToServer(
                StateTransitionPacket.TYPE,
                StateTransitionPacket.STREAM_CODEC,
                (packet, context) -> {
                    // 已经在主线程了，NeoForge 1.21.1默认在主线程处理
                    Player player = context.player();
                    MovementStateMachine stateMachine = player.getData(ModAttachments.MOVEMENT_STATE);
                    stateMachine.setStateFromClient(packet.stateType(), player);
                }
        );
    }
}
