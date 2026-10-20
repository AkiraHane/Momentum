package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.utils.MomentumEventHelper;
import com.akirahane.momentum.platform.ForcedPoseAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements ForcedPoseAccess {

    @Unique
    private @Nullable Pose momentum$forcedPose;

    @Override
    public void momentum$setForcedPose(@Nullable Pose pose) {
        momentum$forcedPose = pose;
    }

    // 在原版姿态更新前应用，避免强制姿态被自动计算覆盖。
    @Inject(method = "updatePlayerPose", at = @At("HEAD"), cancellable = true)
    private void momentum$applyForcedPose(CallbackInfo ci) {
        if (momentum$forcedPose != null) {
            ((Player) (Object) this).setPose(momentum$forcedPose);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void momentum$beforePlayerTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            MomentumEventHelper.onPlayerTick(player);
        }
    }
}
