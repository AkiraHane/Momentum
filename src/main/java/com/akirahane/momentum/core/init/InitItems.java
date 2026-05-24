package com.akirahane.momentum.core.init;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

public class InitItems {
    // 创建一个延迟注册器来持有物品，所有物品都将注册在 "momentum" 命名空间下
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册物品
    public static final DeferredItem<@NotNull Item> JET_BOOSTER_ITEM = ITEMS.registerSimpleItem(
            "jet_booster",
            p -> p.stacksTo(1) // 设置物品可堆叠1
                    .rarity(Rarity.RARE)
    );

    public static void register(IEventBus modEventBus) {
        // 将延迟注册器注册到 mod 事件总线，以便物品被注册
        ITEMS.register(modEventBus);
        // 将物品注册到创造模式标签页
        modEventBus.addListener(InitItems::addCreative);
    }

    // 将示例方块物品添加到战斗标签页
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(JET_BOOSTER_ITEM);
        }
    }
}
