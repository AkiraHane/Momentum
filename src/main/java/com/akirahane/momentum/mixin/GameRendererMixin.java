package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.akirahane.momentum.core.state.states.ground.WalkState.ICE_SLIDE;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void momentum$cancelBobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasData(InitAttachments.MOVEMENT_STATE)) return;

        var machine = mc.player.getData(InitAttachments.MOVEMENT_STATE);
        if (StateType.SLIDE.equals(machine.getCurrentState().getStateType())) {
            ci.cancel();
        }
        if (ICE_SLIDE.equals(machine.getContext().getCurrentAnimationName())) {
            ci.cancel();
        }
    }
}
