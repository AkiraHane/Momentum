package com.akirahane.momentum.client.hud;

import net.minecraft.client.Minecraft;

public class MomentumHudHelper {

    /** 更新提示的淡入淡出，游戏暂停时保持当前状态。 */
    public static void onClientTick(Minecraft client) {
        if (client.player == null || client.level == null || client.isPaused()) return;
        HintManager.clientTick(client.player);
    }
}
