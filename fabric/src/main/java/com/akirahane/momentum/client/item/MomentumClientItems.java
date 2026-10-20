package com.akirahane.momentum.client.item;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class MomentumClientItems implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) ->
                MomentumClientItemHelper.appendTooltip(stack, lines)
        );
    }
}
