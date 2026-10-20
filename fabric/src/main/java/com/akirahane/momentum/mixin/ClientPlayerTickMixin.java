package com.akirahane.momentum.mixin;

import com.akirahane.momentum.client.tick.MomentumClientTickHelper;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 对齐 NeoForge 的 PlayerTickEvent.Pre/Post，覆盖本地和远程玩家。
@Mixin(Player.class)
public abstract class ClientPlayerTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void momentum$beforeClientPlayerTick(CallbackInfo ci) {
        if ((Object) this instanceof AbstractClientPlayer player) {
            MomentumClientTickHelper.onPlayerTickPre(player);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void momentum$afterClientPlayerTick(CallbackInfo ci) {
        if ((Object) this instanceof AbstractClientPlayer player) {
            MomentumClientTickHelper.onPlayerTickPost(player);
        }
    }
}
