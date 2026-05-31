package com.akirahane.momentum.init;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.network.SyncMomentumEnabledPacket;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Momentum.MODID)
public class InitNetwork {

    @SubscribeEvent // on the mod event bus
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 机动模式切换
        registrar.playToServer(
                ToggleMomentumPacket.TYPE,
                ToggleMomentumPacket.STREAM_CODEC,
                (packet, context) -> {
                    Player player = context.player();
                    boolean current = !player.getData(InitAttachments.MOMENTUM_ENABLED);
                    player.setData(InitAttachments.MOMENTUM_ENABLED, current);
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
                    player.setData(InitAttachments.MOMENTUM_ENABLED, packet.enabled());
                    player.sendOverlayMessage(
                            Component.translatable(packet.enabled()
                                    ? "message.momentum.momentum_enabled"
                                    : "message.momentum.momentum_disabled")
                    );
                }
        );

        // 状态机同步
        registrar.playToServer(
                StateTransitionPacket.TYPE,
                StateTransitionPacket.STREAM_CODEC,
                (packet, context) -> {
                    Player player = context.player();
                    MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
                    stateMachine.setStateFromClient(packet.stateType(), player);
                }
        );

        registrar.playToClient(
                StateBroadcastPacket.TYPE,
                StateBroadcastPacket.STREAM_CODEC,
                (packet, context) -> {
                    var mc = Minecraft.getInstance();
                    if (mc.level == null) return;

                    var entity = mc.level.getEntity(packet.playerId());
                    if (!(entity instanceof Player player)) return;
                    if (player == mc.player) return;

                    // 直接设置那个玩家实体上的状态机
                    MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
                    stateMachine.setStateFromClient(packet.stateType(), player);
                }
        );
    }
}
