package com.linqiyinya.momentum.compat.trinkets;

import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;

import static com.linqiyinya.momentum.registry.MomentumItems.JET_BOOSTER_ITEM;

public final class TrinketsHandler {

    private TrinketsHandler() {
    }

    public static boolean hasJetBooster(Player player) {
        // 只识别可生效的装备，排除装饰槽。
        return TrinketsApi.getAttachment(player).isEquipped(JET_BOOSTER_ITEM.get(), true);
    }
}
