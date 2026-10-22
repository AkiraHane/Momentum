package com.linqiyinya.momentum.platform.services;

import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

// TODO: 待平台适配范围明确后，按职责拆分接口。
public interface IPlatformHelper {

    MomentumStateMachine getOrCreateStateMachine(Player player);

    boolean hasStateMachine(Player player);

    boolean isMomentumEnabled(Player player);

    boolean hasAccessoryJetBooster(Player player);

    void setMomentumEnabled(Player player, boolean enabled);

    // null 时恢复原版姿态计算。
    void setForcedPose(Player player, @Nullable Pose pose);

    float getBlockFriction(BlockState state, Player player, BlockPos pos);

    SoundType getBlockSoundType(BlockState state, Player player, BlockPos pos);

    void sendToServer(CustomPacketPayload packet);

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
