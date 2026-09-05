package com.akirahane.momentum.fabric.init;

import com.akirahane.momentum.MomentumConstants;
import com.akirahane.momentum.item.MomentumItemProperties;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class FabricItems {
    public static final ResourceKey<Item> JET_BOOSTER_KEY = ResourceKey.create(
            Registries.ITEM, id("jet_booster"));
    public static final Item JET_BOOSTER = Registry.register(
            BuiltInRegistries.ITEM,
            JET_BOOSTER_KEY,
            new Item(MomentumItemProperties.jetBooster(
                    new Item.Properties().setId(JET_BOOSTER_KEY))));

    private FabricItems() {
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.accept(JET_BOOSTER));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, path);
    }
}
