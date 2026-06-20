package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import static com.akirahane.momentum.client.init.InitKeyMappings.MOMENTUM_CATEGORY;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class LowerCenterKey {
    protected static final Logger LOGGER = LogUtils.getLogger();

    // 按住生效
    public static final Lazy<@NotNull KeyMapping> LOWER_CENTER = Lazy.of(() -> new KeyMapping(
            "key.momentum.lower_center", // 将使用此翻译密钥进行本地化处理
            KeyConflictContext.IN_GAME, // 游戏中
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_C, // 按下C
            MOMENTUM_CATEGORY // 移动类别
    ));
}
