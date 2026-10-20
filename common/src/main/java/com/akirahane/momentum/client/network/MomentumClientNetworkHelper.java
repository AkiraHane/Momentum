package com.akirahane.momentum.client.network;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MomentumClientNetworkHelper {

    public static void onStateBroadcast(StateBroadcastPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var entity = mc.level.getEntity(packet.playerId());
        if (!(entity instanceof Player player)) return;
        if (player == mc.player) return;

        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), player);
    }
}
