package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
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

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean bound = LOWER_CENTER.get().getKey() != InputConstants.UNKNOWN;
        if (!bound) return;
        MovementStateMachine machine = player.getData(InitAttachments.MOVEMENT_STATE);
        machine.getContext().setLowerCenter(LOWER_CENTER.get().isDown());
    }
}
