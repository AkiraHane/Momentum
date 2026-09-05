package com.akirahane.momentum.fabric.mixin.client;

import com.akirahane.momentum.fabric.client.FabricClientVisuals;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Unique
    private float momentum$cameraPartialTick;

    @Unique
    private boolean momentum$aligningWithEntity;

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void momentum$beginCameraAlignment(float partialTick, CallbackInfo ci) {
        this.momentum$cameraPartialTick = partialTick;
        this.momentum$aligningWithEntity = true;
    }

    @Inject(method = "alignWithEntity", at = @At("RETURN"))
    private void momentum$endCameraAlignment(float partialTick, CallbackInfo ci) {
        this.momentum$aligningWithEntity = false;
    }

    @ModifyArg(
            method = "setRotation(FF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"),
            index = 2)
    private float momentum$applyCameraRoll(float originalRoll) {
        if (!this.momentum$aligningWithEntity || !momentum$cameraRollAppliesHere()) {
            return originalRoll;
        }

        float rollRadians = (float) Math.toRadians(
                FabricClientVisuals.cameraRoll(this.momentum$cameraPartialTick));

        // NeoForge's three-angle Camera#setRotation negates degree roll before
        // passing it to JOML. A mirrored third-person camera negates it once more.
        return originalRoll + (Minecraft.getInstance().options.getCameraType().isMirrored()
                ? rollRadians
                : -rollRadians);
    }

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void momentum$applyFovBonus(float partialTick, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() + FabricClientVisuals.fovBonus(partialTick));
    }

    @Unique
    private static boolean momentum$cameraRollAppliesHere() {
        Minecraft minecraft = Minecraft.getInstance();
        var cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            return false;
        }

        if (minecraft.options.getCameraType().isFirstPerson()
                && cameraEntity instanceof LivingEntity living
                && living.isSleeping()) {
            return false;
        }

        return !(cameraEntity.isPassenger()
                && cameraEntity.getVehicle() instanceof Minecart minecart
                && minecart.getBehavior() instanceof NewMinecartBehavior behavior
                && behavior.cartHasPosRotLerp());
    }
}
