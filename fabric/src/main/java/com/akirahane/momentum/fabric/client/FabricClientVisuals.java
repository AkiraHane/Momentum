package com.akirahane.momentum.fabric.client;

import com.akirahane.momentum.client.ClientVisualEffects;
import net.minecraft.client.Minecraft;

public final class FabricClientVisuals {
    private FabricClientVisuals() {
    }

    public static float cameraRoll(float partialTick) {
        return ClientVisualEffects.cameraRoll(Minecraft.getInstance().player, partialTick);
    }

    public static float fovBonus(float partialTick) {
        return ClientVisualEffects.fovBonus(Minecraft.getInstance().player, partialTick);
    }
}
