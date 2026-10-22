package com.linqiyinya.momentum.compat.curios.handler;

import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

import static com.linqiyinya.momentum.registry.MomentumItems.JET_BOOSTER_ITEM;

public final class CuriosHandler {
    public static boolean hasJetBooster(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findFirstCurio(JET_BOOSTER_ITEM.get()).isPresent())
                .orElse(false);
    }
}
