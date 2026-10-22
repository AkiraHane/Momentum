package com.linqiyinya.momentum.platform;

import com.linqiyinya.momentum.compat.trinkets.TrinketsHandler;
import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.registry.MomentumAttachments;
import com.linqiyinya.momentum.platform.services.IPlatformHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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
    public boolean hasAccessoryJetBooster(Player player) {
        return isModLoaded("trinkets_updated") && TrinketsHandler.hasJetBooster(player);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        player.setAttached(MomentumAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void setForcedPose(Player player, @Nullable Pose pose) {
        ((ForcedPoseAccess) player).momentum$setForcedPose(pose);
    }

    @Override
    public float getBlockFriction(BlockState state, Player player, BlockPos pos) {
        return state.getBlock().getFriction();
    }

    @Override
    public SoundType getBlockSoundType(BlockState state, Player player, BlockPos pos) {
        return state.getSoundType();
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
