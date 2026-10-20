package com.akirahane.momentum.client.hud;

import com.akirahane.momentum.Constants;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientHud {

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, HintHudLayer.LAYER_ID, HintHudLayer::render);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        MomentumHudHelper.onClientTick(Minecraft.getInstance());
    }
}
