package com.akirahane.momentum.platform;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.registry.MomentumAttachments;
import com.akirahane.momentum.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public MomentumStateMachine getOrCreateStateMachine(Player player) {
        return player.getAttachedOrCreate(MomentumAttachments.MOMENTUM_STATE, () -> new MomentumStateMachine(player));
    }

    @Override
    public boolean hasStateMachine(Player player) {
        return player.hasAttached(MomentumAttachments.MOMENTUM_STATE);
    }

    @Override
    public boolean isMomentumEnabled(Player player) {
        return player.getAttachedOrCreate(MomentumAttachments.MOMENTUM_ENABLED);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        player.setAttached(MomentumAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void sendToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
