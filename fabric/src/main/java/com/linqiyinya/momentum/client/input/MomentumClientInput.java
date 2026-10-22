package com.linqiyinya.momentum.client.input;

import com.linqiyinya.momentum.client.registry.MomentumKeyMappingRegistration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class MomentumClientInput implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MomentumKeyMappingRegistration.register();
        ClientTickEvents.START_CLIENT_TICK.register(MomentumClientInput::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        MCInputKey.onClientTick();

        var modifier = MomentumKeyMappingRegistration.FUNCTION_MODIFIER.get();
        boolean functionKeysActive = client.player != null && client.screen == null
                && (modifier.isUnbound() || modifier.isDown());
        ChangeMomentumKey.onClientTick(
                functionKeysActive && ChangeMomentumKey.CHANGE_MOMENTUM_KEY_MAPPING.get().isDown()
        );
        ToggleHintKey.onClientTick(
                functionKeysActive && ToggleHintKey.TOGGLE_HINT_KEY_MAPPING.get().isDown()
        );
    }
}
