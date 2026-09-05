package com.akirahane.momentum.fabric.client;

import com.akirahane.momentum.MomentumConstants;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FabricKeyMappings {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, "category"));

    public static final KeyMapping LOWER_CENTER = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.momentum.lower_center", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
    public static final KeyMapping TOGGLE_MOMENTUM = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.momentum.change_momentum", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
    public static final KeyMapping TOGGLE_HINTS = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.momentum.toggle_hint", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY));

    private FabricKeyMappings() {
    }

    public static void register() {
        // Triggers static registration from the Fabric client entrypoint.
    }
}
