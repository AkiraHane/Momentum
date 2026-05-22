package com.akirahane.momentum.handler.curios;

import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

import static com.akirahane.momentum.init.ModItems.JET_BOOSTER_ITEM;

public class CuriosHandler {
    static boolean hasJetBooster(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findFirstCurio(JET_BOOSTER_ITEM.get()).isPresent())
                .orElse(false);
    }
}
