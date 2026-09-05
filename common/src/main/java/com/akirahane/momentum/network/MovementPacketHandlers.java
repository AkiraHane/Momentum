package com.akirahane.momentum.network;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.platform.PlatformServices;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

/**
 * Loader-neutral handling for Momentum's play payloads.
 *
 * <p>Fabric and NeoForge own payload registration and transport. Keeping state
 * mutation here prevents the two loader adapters from drifting apart.</p>
 */
public final class MovementPacketHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private MovementPacketHandlers() {
    }

    public static void handleToggle(ServerPlayer player) {
        boolean enabled = !PlatformServices.gameplay().isMomentumEnabled(player);
        PlatformServices.gameplay().setMomentumEnabled(player, enabled);
        player.sendSystemMessage(Component.translatable(enabled
                ? "message.momentum.momentum_enabled"
                : "message.momentum.momentum_disabled"), true);
    }

    public static void handleStateTransition(ServerPlayer player, StateTransitionPacket packet) {
        if (!hasValidMetadata(packet)) {
            LOGGER.warn("Ignoring invalid Momentum state metadata from {}: state={}, extra={}, wall={}",
                    player.getScoreboardName(), packet.stateType(), packet.extraData(), packet.wallData());
            return;
        }

        MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(player);
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), player);
    }

    public static void handleStateBroadcast(Player receivingPlayer, StateBroadcastPacket packet) {
        Entity entity = receivingPlayer.level().getEntity(packet.playerId());
        if (!(entity instanceof Player remotePlayer) || remotePlayer == receivingPlayer) {
            return;
        }

        MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(remotePlayer);
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), remotePlayer);
    }

    public static StateBroadcastPacket stateSnapshot(Player player) {
        MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(player);
        return new StateBroadcastPacket(
                player.getId(),
                stateMachine.getCurrentState().getStateType(),
                stateMachine.getContext().getTransitionExtraData(),
                stateMachine.getContext().getTransitionWallData());
    }

    private static boolean hasValidMetadata(StateTransitionPacket packet) {
        int extraData = packet.extraData();
        int wallData = packet.wallData();
        return extraData >= -1 && extraData <= 3
                && wallData >= -1 && wallData <= 15;
    }
}
