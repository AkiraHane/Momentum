package com.akirahane.momentum.platform;

import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.registry.MomentumAttachments;
import com.akirahane.momentum.platform.services.IPlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

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
    public boolean hasAccessoryJetBooster(Player player) {
        return CuriosCompat.isLoaded() && CuriosHandler.hasJetBooster(player);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        player.setData(MomentumAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void setForcedPose(Player player, @Nullable Pose pose) {
        player.setForcedPose(pose);
    }

    @Override
    public float getBlockFriction(BlockState state, Player player, BlockPos pos) {
        return state.getFriction(player.level(), pos, player);
    }

    @Override
    public SoundType getBlockSoundType(BlockState state, Player player, BlockPos pos) {
        return state.getSoundType(player.level(), pos, player);
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
