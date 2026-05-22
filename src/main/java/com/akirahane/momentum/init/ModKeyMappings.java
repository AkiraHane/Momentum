package com.akirahane.momentum.init;

import com.akirahane.momentum.Momentum;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

// 此类不会在专用服务器上加载。在此处访问客户端代码是安全的。
@Mod(value = Momentum.MODID, dist = Dist.CLIENT)
// 你可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class ModKeyMappings {
    public static final KeyMapping.Category MOMENTUM_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(Momentum.MODID, "category")
    );
    // 键映射是延迟初始化的，因此在注册之前它并不会存在。
    public static final Lazy<@NotNull KeyMapping> LOWER_CENTER_KEY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.momentum.lower_center", // 将使用此翻译密钥进行本地化处理
            KeyConflictContext.IN_GAME, // 游戏中
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_C, // 按下C
            KeyMapping.Category.MOVEMENT // 移动类别
    ));
    public static final Lazy<@NotNull KeyMapping> CHANGE_MOMENTUM_KEY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.momentum.change_momentum", // 将使用此翻译密钥进行本地化处理
            KeyConflictContext.IN_GAME, // 游戏中
            KeyModifier.SHIFT, // 按下SHIFT
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_M, // 同时按下M
            MOMENTUM_CATEGORY // 自定义类别
    ));

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        // 注册类别
        event.registerCategory(MOMENTUM_CATEGORY);
        // 注册按键绑定
        event.register(LOWER_CENTER_KEY_MAPPING.get());
        event.register(CHANGE_MOMENTUM_KEY_MAPPING.get());
    }
}
