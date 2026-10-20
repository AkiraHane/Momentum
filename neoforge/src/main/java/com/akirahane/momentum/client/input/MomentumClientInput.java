package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientInput {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        MCInputKey.onClientTick();
        ChangeMomentumKey.onClientTick(ChangeMomentumKey.CHANGE_MOMENTUM_KEY_MAPPING.get().isDown());
        ToggleHintKey.onClientTick(ToggleHintKey.TOGGLE_HINT_KEY_MAPPING.get().isDown());
    }
}
