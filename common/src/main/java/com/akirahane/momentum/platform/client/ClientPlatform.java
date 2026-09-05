package com.akirahane.momentum.platform.client;

import net.minecraft.world.entity.player.Player;

/** Client-only operations supplied by the active loader adapter. */
public interface ClientPlatform {
    ClientPlatform NOOP = new ClientPlatform() {
        @Override
        public MovementInput captureMovementInput(Player player) {
            return MovementInput.EMPTY;
        }

        @Override
        public float fovEffectScale() {
            return 1.0F;
        }
    };

    MovementInput captureMovementInput(Player player);

    float fovEffectScale();

    default void clearHints() {
    }

    default void addHint(MovementHint hint) {
    }

    default boolean containsHint(MovementHint hint) {
        return false;
    }

    default boolean isLocalPlayer(Player player) {
        return false;
    }

    default void playSound(Player player, MomentumSound sound, float volume, float pitch) {
    }
}
