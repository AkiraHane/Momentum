package com.akirahane.momentum.client.debug;

import com.akirahane.momentum.Constants;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClientDebug {

    @SubscribeEvent
    public static void register(RegisterDebugEntriesEvent event) {
        event.register(MomentumDebugEntry.ENTRY_ID, new MomentumDebugEntry());
        event.includeInProfile(MomentumDebugEntry.ENTRY_ID, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.NEVER);
    }
}
