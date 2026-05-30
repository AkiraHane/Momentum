package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import static com.akirahane.momentum.client.init.InitKeyMappings.MOMENTUM_CATEGORY;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class ChangeMomentumKey {
    protected static final Logger LOGGER = LogUtils.getLogger();

    private static boolean wasToggleDown = false;

    public static final Lazy<@NotNull KeyMapping> CHANGE_MOMENTUM_KEY_MAPPING = Lazy.of(() -> new KeyMapping(
            "key.momentum.change_momentum", // 将使用此翻译密钥进行本地化处理
            KeyConflictContext.IN_GAME, // 游戏中
            KeyModifier.SHIFT, // 按下SHIFT
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_M, // 同时按下M
            MOMENTUM_CATEGORY // 自定义类别
    ));

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        // 动量模式切换 - 单次触发
        boolean isDown = CHANGE_MOMENTUM_KEY_MAPPING.get().isDown();
        if (isDown && !wasToggleDown) {
            ClientPacketDistributor.sendToServer(new ToggleMomentumPacket());
        }
        wasToggleDown = isDown;
    }
}
