package com.akirahane.momentum.mixin;

import com.akirahane.momentum.client.render.MomentumClientRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    // 在单只手的矩阵保存后应用，原版 popPose 会恢复，双手的变换互相独立。
    @Inject(method = "renderArmWithItem", at = @At(
            value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
            ordinal = 0, shift = At.Shift.AFTER
    ))
    private void momentum$applyHandTransform(
            AbstractClientPlayer player, float frameInterp, float xRot, InteractionHand hand, float attack,
            ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci
    ) {
        MomentumClientRenderHelper.applyHandTransform(poseStack, frameInterp);
    }
}
