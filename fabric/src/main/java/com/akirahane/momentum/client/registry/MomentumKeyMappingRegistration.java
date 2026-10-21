package com.akirahane.momentum.client.registry;

import com.akirahane.momentum.client.input.ChangeMomentumKey;
import com.akirahane.momentum.client.input.LowerCenterKey;
import com.akirahane.momentum.client.input.ToggleHintKey;
import com.akirahane.momentum.client.input.WallInteractionKey;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.function.Supplier;

public class MomentumKeyMappingRegistration {
    public static final String FUNCTION_MODIFIER_KEY_NAME = "key.momentum.function_modifier";
    public static final Supplier<KeyMapping> FUNCTION_MODIFIER = () -> KeyMapping.get(FUNCTION_MODIFIER_KEY_NAME);

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(MomentumKeyMappings.MOMENTUM_CATEGORY_ID);
        KeyMappingHelper.registerKeyMapping(new KeyMapping(
                LowerCenterKey.KEY_NAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category
        ));
        KeyMappingHelper.registerKeyMapping(new KeyMapping(
                WallInteractionKey.KEY_NAME, InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), category
        ));
        KeyMappingHelper.registerKeyMapping(new KeyMapping(
                ChangeMomentumKey.KEY_NAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category
        ));
        KeyMappingHelper.registerKeyMapping(new KeyMapping(
                ToggleHintKey.KEY_NAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, category
        ));
        KeyMappingHelper.registerKeyMapping(new KeyMapping(
                FUNCTION_MODIFIER_KEY_NAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_SHIFT, category
        ));
    }
}
