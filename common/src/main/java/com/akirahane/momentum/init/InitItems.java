package com.akirahane.momentum.init;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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
            p -> p.stacksTo(1)
                    .rarity(Rarity.RARE)
                    .enchantable(22)
                    .equippable(EquipmentSlot.LEGS)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ARMOR,
                                    new AttributeModifier(Identifier.fromNamespaceAndPath(MODID, "jet_booster_armor"),
                                            5.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.LEGS)
                            .add(Attributes.ARMOR_TOUGHNESS,
                                    new AttributeModifier(Identifier.fromNamespaceAndPath(MODID, "jet_booster_toughness"),
                                            2.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.LEGS)
                            .build())
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
