package com.linqiyinya.momentum.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Supplier;

import static com.linqiyinya.momentum.Constants.MOD_ID;

public class MomentumItems {

    public static final ResourceKey<Item> JET_BOOSTER_KEY = ResourceKey.create(
            Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "jet_booster")
    );

    // 延迟查找，避免在平台注册前读取物品。
    public static final Supplier<Item> JET_BOOSTER_ITEM = () -> BuiltInRegistries.ITEM.getValueOrThrow(JET_BOOSTER_KEY);

    public static Item.Properties createJetBoosterProperties() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .enchantable(22)
                .equippable(EquipmentSlot.LEGS)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                                new AttributeModifier(Identifier.fromNamespaceAndPath(MOD_ID, "jet_booster_armor"),
                                        5.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.LEGS)
                        .add(Attributes.ARMOR_TOUGHNESS,
                                new AttributeModifier(Identifier.fromNamespaceAndPath(MOD_ID, "jet_booster_toughness"),
                                        2.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.LEGS)
                        .build());
    }
}
