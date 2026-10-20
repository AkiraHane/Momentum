package com.akirahane.momentum.core.utils;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;

public class MomentumEventHelper {

    public static void onPlayerLogin(ServerPlayer player, boolean enabled) {
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

    public static void onServerTick(MinecraftServer server, BiConsumer<ServerPlayer, StateBroadcastPacket> sendPacket) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
            if (stateMachine.isDirty()) {
                StateBroadcastPacket packet = new StateBroadcastPacket(
                        player.getId(),
                        stateMachine.getCurrentState().getStateType(),
                        stateMachine.getContext().getTransitionExtraData(),
                        stateMachine.getContext().getTransitionWallData()
                );
                // 包含玩家自身，供 Replay Mod 录制状态转换。
                for (ServerPlayer observer : player.level().players()) {
                    sendPacket.accept(observer, packet);
                }
                stateMachine.setDirty(false);
            }
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
