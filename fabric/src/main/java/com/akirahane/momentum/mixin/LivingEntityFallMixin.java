package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.utils.MomentumEventHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFallMixin {

    @WrapOperation(
            method = "causeFallDamage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z")
    )
    private boolean momentum$adjustFallDamage(
            LivingEntity self, double distance, float multiplier, DamageSource source, Operation<Boolean> original,
            @Local(name = "effectiveFallDistance") LocalDoubleRef effectiveFallDistance,
            @Local(argsOnly = true) LocalFloatRef damageModifier
    ) {
        if (!(self instanceof Player player)) {
            return original.call(self, distance, multiplier, source);
        }

        MomentumEventHelper.FallResult result = MomentumEventHelper.onLivingFall(player, distance, multiplier);
        effectiveFallDistance.set(result.distance());
        damageModifier.set(result.damageMultiplier());
        return original.call(self, (double) result.distance(), result.damageMultiplier(), source);
    }
}
