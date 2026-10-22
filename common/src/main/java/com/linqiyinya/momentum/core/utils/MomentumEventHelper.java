package com.linqiyinya.momentum.core.utils;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.network.MomentumNetworkHelper;
import com.linqiyinya.momentum.network.StateBroadcastPacket;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class MomentumEventHelper {

    public static void onPlayerLogin(ServerPlayer player, boolean enabled) {
        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("[DATA] LOGIN side=server player={} name={} uuid={} dimension={} enabled={}",
                    player.getId(), player.getName().getString(), player.getUUID(), player.level().dimension().identifier(), enabled);
        }
        player.sendSystemMessage(
                Component.translatable(enabled
                        ? "message.momentum.momentum_enabled"
                        : "message.momentum.momentum_disabled"),
                true
        );
    }

    public static void onPlayerTick(ServerPlayer player) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        stateMachine.serverTick(player);
    }

    public static void onServerTick(MinecraftServer server, BiConsumer<ServerPlayer, StateBroadcastPacket> broadcastState) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
            if (stateMachine.isDirty()) {
                StateBroadcastPacket packet = MomentumNetworkHelper.stateSnapshot(player);
                broadcastState.accept(player, packet);
                stateMachine.setDirty(false);
                Constants.LOG.debug("[NET] BROADCAST side=server player={} dimension={} state={} extra={} wall={} audience=tracking+self dirty={}",
                        player.getId(), player.level().dimension().identifier(), packet.stateType(), packet.extraData(), packet.wallData(), stateMachine.isDirty());
            }
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("[DATA] RESPAWN side=server player={} name={} uuid={} dimension={} enabled={}",
                    player.getId(), player.getName().getString(), player.getUUID(), player.level().dimension().identifier(),
                    Services.PLATFORM.isMomentumEnabled(player));
        }
    }

    public static FallResult onLivingFall(Player player, double originalDistance, float damageMultiplier) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        float distance = (float) originalDistance;
        float multiplier = damageMultiplier;

        if (stateMachine.getContext().isHasJetBooster()) {
            distance -= 12;
            multiplier *= 0.5F;
        }
        if (stateMachine.getContext().getBreakFallReadyCount() > 0) {
            distance -= 6;
            multiplier *= 0.3F;
            if (originalDistance > Math.min(4, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))) {
                stateMachine.getContext().setToBreakFallState(true);
            }
        } else if (stateMachine.getContext().getBreakFallReadyCount() == 0) {
            distance -= 3;
            multiplier *= 0.6F;
            if (originalDistance > Math.min(4, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))) {
                stateMachine.getContext().setToBreakFallState(true);
            }
        }

        return new FallResult(Math.max(0, distance), multiplier);
    }

    public static boolean onPlayerHurt(Player player) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        if (StateType.DODGE.equals(stateMachine.getCurrentState().getStateType())) {
            player.fallDistance = 0;
            return true;
        }
        return false;
    }

    public record FallResult(float distance, float damageMultiplier) {
    }
}
