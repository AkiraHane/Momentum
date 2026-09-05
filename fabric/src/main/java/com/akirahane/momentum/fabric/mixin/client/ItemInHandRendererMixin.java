package com.akirahane.momentum.fabric.mixin.client;

import com.akirahane.momentum.fabric.client.FabricClientVisuals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void momentum$transformHands(
            float partialTick,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            LocalPlayer player,
            int light,
            CallbackInfo ci) {
        FabricClientVisuals.transformHands(poseStack, partialTick);
    }
}
