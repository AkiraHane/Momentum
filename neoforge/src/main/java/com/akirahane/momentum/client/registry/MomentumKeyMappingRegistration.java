package com.akirahane.momentum.client.registry;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.client.input.ChangeMomentumKey;
import com.akirahane.momentum.client.input.LowerCenterKey;
import com.akirahane.momentum.client.input.ToggleHintKey;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumKeyMappingRegistration {

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(MomentumKeyMappings.MOMENTUM_CATEGORY_ID);
        event.registerCategory(category);
        event.register(new KeyMapping(
                LowerCenterKey.KEY_NAME, KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category
        ));
        event.register(new KeyMapping(
                ChangeMomentumKey.KEY_NAME, KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, category
        ));
        event.register(new KeyMapping(
                ToggleHintKey.KEY_NAME, KeyConflictContext.IN_GAME, KeyModifier.SHIFT,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, category
        ));
    }
}
