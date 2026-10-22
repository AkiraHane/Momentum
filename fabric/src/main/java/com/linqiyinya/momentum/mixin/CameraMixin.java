package com.linqiyinya.momentum.mixin;

import com.linqiyinya.momentum.client.render.MomentumClientRenderHelper;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow @Final private static Vector3f FORWARDS;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f LEFT;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow private int matrixPropertiesDirty;

    @Unique private float momentum$cameraRoll;

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void momentum$resetCameraRoll(float partialTicks, CallbackInfo ci) {
        momentum$cameraRoll = 0F;
    }

    // 第 0 次 setRotation 属于矿车插值；第 1 次为 NeoForge 触发相机事件的普通跟随分支。
    @Inject(method = "alignWithEntity", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
            ordinal = 1, shift = At.Shift.AFTER
    ))
    private void momentum$applyCameraRoll(float partialTicks, CallbackInfo ci) {
        momentum$cameraRoll = MomentumClientRenderHelper.getCameraRoll(partialTicks);
        momentum$rotateCamera(momentum$cameraRoll);
    }

    // 第三人称前视重设朝向后，反转本次倾斜，与 NeoForge 保持一致。
    @Inject(method = "alignWithEntity", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V",
            ordinal = 2, shift = At.Shift.AFTER
    ))
    private void momentum$applyMirroredCameraRoll(float partialTicks, CallbackInfo ci) {
        momentum$rotateCamera(-momentum$cameraRoll);
    }

    @Unique
    private void momentum$rotateCamera(float roll) {
        if (roll == 0F) return;
        rotation.rotateZ(-roll * ((float) Math.PI / 180F));
        FORWARDS.rotate(rotation, forwards);
        UP.rotate(rotation, up);
        LEFT.rotate(rotation, left);
        // 朝向向量和视图矩阵缓存须随旋转一起更新。
        matrixPropertiesDirty |= 3;
    }

    // 对齐 ComputeFov：在原版死亡、水下修正后，为世界和手部视野添加增量。
    @ModifyReturnValue(method = "modifyFovBasedOnDeathOrFluid", at = @At("RETURN"))
    private float momentum$addFovBonus(float original, float partialTicks, float fov) {
        float bonus = MomentumClientRenderHelper.getFovBonus(partialTicks);
        return bonus == 0F ? original : original + bonus;
    }
}
