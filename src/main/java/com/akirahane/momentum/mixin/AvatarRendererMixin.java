package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @ModifyVariable(
            method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("STORE"),
            name = "swimAmount")
    private float setupRotations(float swimAmount, @Local(argsOnly = true, name = "state") AvatarRenderState state) {
        if (Minecraft.getInstance().level == null) {
            return swimAmount;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(state.id);
        if (!(entity instanceof Player player)) {
            return swimAmount;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return swimAmount;
        }
        if (stateMachine.getCurrentState().getStateType().equals(StateType.SLIDE)) {
            return 0.0F;
        }
        return swimAmount;
    }
}
