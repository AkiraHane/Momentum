package com.akirahane.momentum.fabric.compat.trinkets;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;

/** Optional Trinkets Updated integration entrypoint without hard API linkage. */
public final class FabricTrinketsCompat {
    private static final String TRINKETS_MOD_ID = "trinkets_updated";

    private FabricTrinketsCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(TRINKETS_MOD_ID);
    }

    public static boolean hasJetBooster(Player player) {
        return isLoaded() && FabricTrinketsHandler.hasJetBooster(player);
    }
}
