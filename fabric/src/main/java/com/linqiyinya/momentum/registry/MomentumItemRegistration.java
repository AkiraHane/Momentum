package com.linqiyinya.momentum.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class MomentumItemRegistration {

    public static void register() {
        Item jetBooster = Registry.register(
                BuiltInRegistries.ITEM,
                MomentumItems.JET_BOOSTER_KEY,
                new Item(MomentumItems.createJetBoosterProperties().setId(MomentumItems.JET_BOOSTER_KEY))
        );
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.accept(jetBooster));
    }
}
