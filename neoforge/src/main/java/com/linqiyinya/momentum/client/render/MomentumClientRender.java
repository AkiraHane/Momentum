package com.linqiyinya.momentum.client.render;

import com.linqiyinya.momentum.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientRender {

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float roll = MomentumClientRenderHelper.getCameraRoll((float) event.getPartialTick());
        if (roll != 0F) {
            event.setRoll(event.getRoll() + roll);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float bonus = MomentumClientRenderHelper.getFovBonus((float) event.getPartialTick());
        if (bonus != 0F) {
            event.setFOV(event.getFOV() + bonus);
        }
    }
}
