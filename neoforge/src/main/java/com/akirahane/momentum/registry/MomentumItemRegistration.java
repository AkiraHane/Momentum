package com.akirahane.momentum.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Constants.MOD_ID;

public class MomentumItemRegistration {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final DeferredItem<@NotNull Item> JET_BOOSTER_ITEM = ITEMS.registerSimpleItem(
            MomentumItems.JET_BOOSTER_KEY.identifier().getPath(),
            MomentumItems::createJetBoosterProperties
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(MomentumItemRegistration::addCreative);
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(JET_BOOSTER_ITEM);
        }
    }
}
