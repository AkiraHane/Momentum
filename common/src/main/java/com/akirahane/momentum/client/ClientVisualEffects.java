package com.akirahane.momentum.client;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.platform.PlatformServices;
import com.akirahane.momentum.platform.config.MomentumClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;

/** Loader-neutral calculations for first-person movement visuals. */
public final class ClientVisualEffects {
    private ClientVisualEffects() {
    }

    public static float cameraRoll(LocalPlayer player, float partialTick) {
        if (!MomentumClientConfig.ENABLE_CAMERA_OFFSET.get() || !isMovementVisualActive(player)) {
            return 0.0F;
        }

        var context = PlatformServices.gameplay().movementState(player).getContext();
        return context.getRenderCameraRoll(partialTick)
                + context.getRenderMomentumRoll(partialTick);
    }

    public static float fovBonus(LocalPlayer player, float partialTick) {
        if (!MomentumClientConfig.ENABLE_CAMERA_OFFSET.get() || !isMovementVisualActive(player)) {
            return 0.0F;
        }

        return PlatformServices.gameplay().movementState(player).getContext()
                .getRenderFovBonus(partialTick);
    }

    public static void transformHands(LocalPlayer player, PoseStack poseStack, float partialTick) {
        if (!isMovementVisualActive(player)) {
            return;
        }

        var context = PlatformServices.gameplay().movementState(player).getContext();
        poseStack.translate(0.0, context.getRenderArmOffsetY(partialTick), 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(context.getRenderArmRotX(partialTick)));
    }

    private static boolean isMovementVisualActive(LocalPlayer player) {
        if (player == null || !PlatformServices.gameplay().hasMovementState(player)) {
            return false;
        }

        return PlatformServices.gameplay().movementState(player).getCurrentState().getStateType()
                != StateType.ORIGINAL;
    }
}
