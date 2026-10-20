package com.akirahane.momentum.client.tick;

import com.akirahane.momentum.Constants;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientTick {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof AbstractClientPlayer player) {
            MomentumClientTickHelper.onPlayerTickPre(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractClientPlayer player) {
            MomentumClientTickHelper.onPlayerTickPost(player);
        }
    }
}
