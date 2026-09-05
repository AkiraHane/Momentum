package com.akirahane.momentum.fabric.network;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.fabric.config.FabricServerConfig;
import com.akirahane.momentum.network.MovementPacketHandlers;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import com.akirahane.momentum.platform.PlatformServices;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Fabric transport adapter for Momentum's shared play protocol. */
public final class FabricNetwork {
    private FabricNetwork() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                ToggleMomentumPacket.TYPE, ToggleMomentumPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(
                StateTransitionPacket.TYPE, StateTransitionPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                StateBroadcastPacket.TYPE, StateBroadcastPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                FabricServerConfigPacket.TYPE, FabricServerConfigPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ToggleMomentumPacket.TYPE,
                (packet, context) -> MovementPacketHandlers.handleToggle(context.player()));
        ServerPlayNetworking.registerGlobalReceiver(StateTransitionPacket.TYPE,
                (packet, context) -> MovementPacketHandlers.handleStateTransition(context.player(), packet));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            sendServerConfig(player);
            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    PlatformServices.gameplay().isMomentumEnabled(player)
                            ? "message.momentum.momentum_enabled"
                            : "message.momentum.momentum_disabled"), true);
        });

        EntityTrackingEvents.START_TRACKING.register((entity, observer) -> {
            if (entity instanceof Player player) {
                send(observer, MovementPacketHandlers.stateSnapshot(player));
            }
        });
    }

    public static void flushDirtyStates(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(player);
            if (!stateMachine.isDirty()) {
                continue;
            }

            StateBroadcastPacket packet = MovementPacketHandlers.stateSnapshot(player);
            for (ServerPlayer observer : PlayerLookup.tracking(player)) {
                send(observer, packet);
            }
            // Keep the self copy for replay recorders. The normal client handler ignores it.
            send(player, packet);
            stateMachine.setDirty(false);
        }
    }

    public static void syncServerConfig(MinecraftServer server) {
        FabricServerConfigPacket packet = new FabricServerConfigPacket(FabricServerConfig.synchronizedJson());
        for (ServerPlayer player : PlayerLookup.all(server)) {
            send(player, packet);
        }
    }

    private static void sendServerConfig(ServerPlayer player) {
        send(player, new FabricServerConfigPacket(FabricServerConfig.synchronizedJson()));
    }

    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (ServerPlayNetworking.canSend(player, payload.type())) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
