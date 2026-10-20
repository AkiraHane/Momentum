package com.akirahane.momentum.platform;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.registry.MomentumAttachments;
import com.akirahane.momentum.platform.services.IPlatformHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public MomentumStateMachine getOrCreateStateMachine(Player player) {
        return player.getData(MomentumAttachments.MOMENTUM_STATE);
    }

    @Override
    public boolean hasStateMachine(Player player) {
        return player.hasData(MomentumAttachments.MOMENTUM_STATE);
    }

    @Override
    public boolean isMomentumEnabled(Player player) {
        return player.getData(MomentumAttachments.MOMENTUM_ENABLED);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        player.setData(MomentumAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void sendToServer(CustomPacketPayload packet) {
        ClientPacketDistributor.sendToServer(packet);
    }

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.getCurrent().isProduction();
    }
}
