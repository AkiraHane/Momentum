package com.akirahane.momentum.fabric.compat.trinkets;

import com.akirahane.momentum.fabric.init.FabricItems;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;

/** Kept separate so Trinkets API classes are only resolved when the mod is loaded. */
final class FabricTrinketsHandler {
    private FabricTrinketsHandler() {
    }

    static boolean hasJetBooster(Player player) {
        return TrinketsApi.getAttachment(player).isEquipped(FabricItems.JET_BOOSTER, true);
    }
}
