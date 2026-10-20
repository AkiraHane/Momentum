package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.utils.MomentumEventHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void momentum$beforePlayerTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            MomentumEventHelper.onPlayerTick(player);
        }
    }
}
