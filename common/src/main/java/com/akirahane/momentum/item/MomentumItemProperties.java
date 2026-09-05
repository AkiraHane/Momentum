package com.akirahane.momentum.item;

import com.akirahane.momentum.MomentumConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class MomentumItemProperties {
    private MomentumItemProperties() {
    }

    public static Item.Properties jetBooster(Item.Properties properties) {
        return properties
                .stacksTo(1)
                .rarity(Rarity.RARE)
                .enchantable(22)
                .equippable(EquipmentSlot.LEGS)
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                                new AttributeModifier(id("jet_booster_armor"), 5.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.LEGS)
                        .add(Attributes.ARMOR_TOUGHNESS,
                                new AttributeModifier(id("jet_booster_toughness"), 2.0,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.LEGS)
                        .build());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, path);
    }
}
