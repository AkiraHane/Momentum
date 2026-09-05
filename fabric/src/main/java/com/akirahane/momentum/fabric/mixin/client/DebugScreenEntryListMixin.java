package com.akirahane.momentum.fabric.mixin.client;

import com.akirahane.momentum.client.debug.MovementDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntryList.class)
public abstract class DebugScreenEntryListMixin {
    @Shadow
    @Final
    private Map<Identifier, DebugScreenEntryStatus> allStatuses;

    @Inject(method = "resetStatuses", at = @At("TAIL"))
    private void momentum$includeMovementEntry(
            Map<Identifier, DebugScreenEntryStatus> statuses, CallbackInfo ci) {
        allStatuses.putIfAbsent(MovementDebugEntry.ID, DebugScreenEntryStatus.NEVER);
    }
}
