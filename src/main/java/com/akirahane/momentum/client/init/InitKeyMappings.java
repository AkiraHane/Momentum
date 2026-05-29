package com.akirahane.momentum.client.init;

import com.akirahane.momentum.Momentum;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static com.akirahane.momentum.client.input.ChangeMomentumKey.CHANGE_MOMENTUM_KEY_MAPPING;
import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER_HOLD;

// 可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class InitKeyMappings {
    public static final KeyMapping.Category MOMENTUM_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(Momentum.MODID, "category")
    );

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        // 注册类别
        event.registerCategory(MOMENTUM_CATEGORY);
        // 注册按键绑定
        event.register(LOWER_CENTER_HOLD.get());
        event.register(CHANGE_MOMENTUM_KEY_MAPPING.get());
    }
}
