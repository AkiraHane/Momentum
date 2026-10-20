package com.akirahane.momentum.client.item;

import com.akirahane.momentum.registry.MomentumItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MomentumClientItemHelper {

    public static void appendTooltip(ItemStack stack, List<Component> lines) {
        if (stack.is(MomentumItems.JET_BOOSTER_ITEM.get())) {
            lines.add(Component.translatable("item.momentum.jet_booster.tooltip"));
        }
    }
}
