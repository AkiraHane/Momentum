package com.linqiyinya.momentum.client.hud;

import com.linqiyinya.momentum.client.registry.MomentumKeyMappingRegistration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class MomentumClientHud implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 与 NeoForge 一致，将提示层放在快捷栏上方。
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, HintHudLayer.LAYER_ID,
                (graphics, delta) -> HintHudLayer.render(
                        graphics, delta, MomentumKeyMappingRegistration.FUNCTION_MODIFIER.get()
                ));
        ClientTickEvents.END_CLIENT_TICK.register(MomentumHudHelper::onClientTick);
    }
}
