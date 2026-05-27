package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.init.InitAttachments;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import static com.akirahane.momentum.client.init.InitKeyMappings.MOMENTUM_CATEGORY;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class LowerCenterKey {

    private static boolean wasToggleDown = false;

    // 按住生效
    public static final Lazy<@NotNull KeyMapping> LOWER_CENTER_HOLD_KEY = Lazy.of(() -> new KeyMapping(
            "key.momentum.lower_center_hold", // 将使用此翻译密钥进行本地化处理
            KeyConflictContext.IN_GAME, // 游戏中
            InputConstants.Type.KEYSYM, // 默认键盘键位
            GLFW.GLFW_KEY_C, // 按下C
            MOMENTUM_CATEGORY // 移动类别
    ));

    // 切换开关，默认未绑定
    public static final Lazy<@NotNull KeyMapping> LOWER_CENTER_TOGGLE_KEY = Lazy.of(() -> new KeyMapping(
            "key.momentum.lower_center_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN, // 默认不设置
            MOMENTUM_CATEGORY
    ));

    // 每tick检测
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        boolean holdBound = LOWER_CENTER_HOLD_KEY.get().getKey() != InputConstants.UNKNOWN;
        boolean toggleBound = LOWER_CENTER_TOGGLE_KEY.get().getKey() != InputConstants.UNKNOWN;
        if (!holdBound && !toggleBound) return;
        MovementStateMachine machine = player.getData(InitAttachments.MOVEMENT_STATE);
        boolean lower_center = machine.getContext().isLowerCenter();
        if (holdBound) {
            // hold 优先
            lower_center = LOWER_CENTER_HOLD_KEY.get().isDown();
        } else if (LOWER_CENTER_TOGGLE_KEY.get().isDown() && !wasToggleDown) {
            lower_center = !lower_center;
        }
        wasToggleDown = LOWER_CENTER_TOGGLE_KEY.get().isDown();
        if (lower_center != machine.getContext().isLowerCenter()){
            machine.getContext().setLowerCenter(lower_center);
        }
    }
}
