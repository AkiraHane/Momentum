package com.akirahane.momentum.init;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.network.MovementPacketHandlers;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** NeoForge transport adapter for Momentum's shared play protocol. */
@EventBusSubscriber(modid = Momentum.MODID)
public final class InitNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private InitNetwork() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                ToggleMomentumPacket.TYPE,
                ToggleMomentumPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        MovementPacketHandlers.handleToggle(player);
                    }
                });

        registrar.playToServer(
                StateTransitionPacket.TYPE,
                StateTransitionPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (context.player() instanceof ServerPlayer player) {
                        MovementPacketHandlers.handleStateTransition(player, packet);
                    }
                });

        registrar.playToClient(
                StateBroadcastPacket.TYPE,
                StateBroadcastPacket.STREAM_CODEC,
                (packet, context) -> MovementPacketHandlers.handleStateBroadcast(context.player(), packet));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer
                && event.getTarget() instanceof Player trackedPlayer) {
            PacketDistributor.sendToPlayer(
                    observer,
                    MovementPacketHandlers.stateSnapshot(trackedPlayer)
            );
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
            if (!stateMachine.isDirty()) {
                continue;
            }

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                    player, MovementPacketHandlers.stateSnapshot(player));
            stateMachine.setDirty(false);
        }
    }
}
