package com.akirahane.momentum.init;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
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
                    // 发一个提示消息给玩家
                    if (player instanceof ServerPlayer sp) {
                        sp.sendSystemMessage(
                                Component.translatable(current
                                        ? "message.momentum.momentum_enabled"
                                        : "message.momentum.momentum_disabled"),
                                true
                        );
                    }
                }
        );

        // 状态机同步
        registrar.playToServer(
                StateTransitionPacket.TYPE,
                StateTransitionPacket.STREAM_CODEC,
                (packet, context) -> {
                    Player player = context.player();
                    MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
                    // 将客户端独有数据注入 context, 供 onEnter() 服务端侧使用
                    stateMachine.getContext().setTransitionExtraData(packet.extraData());
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
                    // 注入附加数据 (如 Dodge 方向), 供 onEnter() 在远程客户端使用
                    stateMachine.getContext().setTransitionExtraData(packet.extraData());
                    stateMachine.setStateFromClient(packet.stateType(), player);
                }
        );
    }
}
