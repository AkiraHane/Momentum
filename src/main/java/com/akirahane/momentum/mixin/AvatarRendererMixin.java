package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    public void momentum$modifyRenderState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        Entity e;
        if (Minecraft.getInstance().level != null) {
            e = Minecraft.getInstance().level.getEntity(state.id);
        } else {
            return;
        }
        if (!(e instanceof Player player)) return;

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.SWIM)) {
            return;
        }
        if (stateMachine.getCurrentState().getStateType().equals(StateType.PRONE)) {
            return;
        }
        if (!stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            state.swimAmount = 0.0F;
        }
    }
}
