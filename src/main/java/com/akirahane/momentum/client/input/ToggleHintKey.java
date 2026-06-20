package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import static com.akirahane.momentum.client.init.InitKeyMappings.MOMENTUM_CATEGORY;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class ToggleHintKey {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private static boolean wasDown = false;

    public static final Lazy<@NotNull KeyMapping> TOGGLE_HINT_KEY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.momentum.toggle_hint",
            KeyConflictContext.IN_GAME,
            KeyModifier.SHIFT, // 按下SHIFT
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_N, // 同时按下N
            MOMENTUM_CATEGORY // 自定义类别
    ));

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        boolean isDown = TOGGLE_HINT_KEY_MAPPING.get().isDown();
        if (isDown && !wasDown) {
            HintManager.toggleVisible();
        }
        wasDown = isDown;
    }

}
