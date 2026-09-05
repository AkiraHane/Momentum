package com.akirahane.momentum.fabric.mixin;

import com.akirahane.momentum.fabric.platform.PlayerForcedPoseAccess;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerForcedPoseMixin implements PlayerForcedPoseAccess {
    @Unique
    private Pose momentum$forcedPose;

    @Override
    public void momentum$setForcedPose(Pose pose) {
        momentum$forcedPose = pose;
    }

    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void momentum$applyForcedPose(CallbackInfo ci) {
        if (momentum$forcedPose != null) {
            ((Player) (Object) this).setPose(momentum$forcedPose);
            ci.cancel();
        }
    }
}
