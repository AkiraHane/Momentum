package com.akirahane.momentum.client.item;

import com.akirahane.momentum.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientItems {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        MomentumClientItemHelper.appendTooltip(event.getItemStack(), event.getToolTip());
    }
}
