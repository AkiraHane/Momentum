package com.akirahane.momentum.fabric.mixin.client;

import com.akirahane.momentum.fabric.client.FabricClientVisuals;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyArg(
            method = "setRotation",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"),
            index = 2)
    private float momentum$applyCameraRoll(float originalRoll) {
        return originalRoll + (float) Math.toRadians(FabricClientVisuals.cameraRoll());
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void momentum$applyFovBonus(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() + FabricClientVisuals.fovBonus());
    }
}
