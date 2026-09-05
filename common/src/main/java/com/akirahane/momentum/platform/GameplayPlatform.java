package com.akirahane.momentum.platform;

import com.akirahane.momentum.core.state.MovementStateMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/** Loader-owned gameplay hooks that cannot be expressed with vanilla APIs. */
public interface GameplayPlatform {
    GameplayPlatform NOOP = new GameplayPlatform() {
        @Override
        public boolean hasJetBooster(Player player) {
            return false;
        }

        @Override
        public float jumpPower(Player player) {
            return 0.0F;
        }

        @Override
        public MovementStateMachine movementState(Player player) {
            throw new IllegalStateException("Gameplay platform is not installed");
        }

        @Override
        public boolean hasMovementState(Player player) {
            return false;
        }

        @Override
        public boolean isMomentumEnabled(Player player) {
            return false;
        }

        @Override
        public void setMomentumEnabled(Player player, boolean enabled) {
            throw new IllegalStateException("Gameplay platform is not installed");
        }

        @Override
        public void setForcedPose(Player player, Pose pose) {
            throw new IllegalStateException("Gameplay platform is not installed");
        }

        @Override
        public SoundType soundType(BlockState state, Level level, BlockPos pos, Player player) {
            throw new IllegalStateException("Gameplay platform is not installed");
        }

        @Override
        public float blockFriction(BlockState state, Level level, BlockPos pos, Player player) {
            throw new IllegalStateException("Gameplay platform is not installed");
        }
    };

    boolean hasJetBooster(Player player);

    float jumpPower(Player player);

    MovementStateMachine movementState(Player player);

    boolean hasMovementState(Player player);

    boolean isMomentumEnabled(Player player);

    void setMomentumEnabled(Player player, boolean enabled);

    void setForcedPose(Player player, Pose pose);

    SoundType soundType(BlockState state, Level level, BlockPos pos, Player player);

    float blockFriction(BlockState state, Level level, BlockPos pos, Player player);
}
