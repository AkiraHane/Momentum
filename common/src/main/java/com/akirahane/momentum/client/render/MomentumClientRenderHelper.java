package com.akirahane.momentum.client.render;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class MomentumClientRenderHelper {

    public static float getCameraRoll(float partialTick) {
        if (!ClientConfig.ENABLE_CAMERA_OFFSET.get()) return 0F;
        PlayerMomentumContext context = getContext();
        if (context == null) return 0F;
        return context.getRenderCameraRoll(partialTick) + context.getRenderMomentumRoll(partialTick);
    }

    public static float getFovBonus(float partialTick) {
        if (!ClientConfig.ENABLE_CAMERA_OFFSET.get()) return 0F;
        PlayerMomentumContext context = getContext();
        return context == null ? 0F : context.getRenderFovBonus(partialTick);
    }

    public static void applyHandTransform(PoseStack poseStack, float partialTick) {
        PlayerMomentumContext context = getContext();
        if (context == null) return;
        poseStack.translate(0, context.getRenderArmOffsetY(partialTick), 0);
        poseStack.mulPose(Axis.XP.rotationDegrees(context.getRenderArmRotX(partialTick)));
    }

    /** 渲染只读取已存在的本地机动状态。 */
    private static @Nullable PlayerMomentumContext getContext() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !Services.PLATFORM.hasStateMachine(mc.player)) return null;
        var stateMachine = Services.PLATFORM.getOrCreateStateMachine(mc.player);
        if (stateMachine.getCurrentState().getStateType() == StateType.ORIGINAL) return null;
        return stateMachine.getContext();
    }
}
