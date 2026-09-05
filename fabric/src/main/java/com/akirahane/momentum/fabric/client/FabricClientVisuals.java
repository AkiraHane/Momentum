package com.akirahane.momentum.fabric.client;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.platform.PlatformServices;
import com.akirahane.momentum.platform.config.MomentumClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class FabricClientVisuals {
    private FabricClientVisuals() {
    }

    public static float cameraRoll() {
        if (!MomentumClientConfig.ENABLE_CAMERA_OFFSET.get()) {
            return 0.0F;
        }
        LocalPlayer player = activePlayer();
        if (player == null) {
            return 0.0F;
        }
        float partialTick = partialTick();
        var context = PlatformServices.gameplay().movementState(player).getContext();
        return context.getRenderCameraRoll(partialTick)
                + context.getRenderMomentumRoll(partialTick);
    }

    public static float fovBonus() {
        if (!MomentumClientConfig.ENABLE_CAMERA_OFFSET.get()) {
            return 0.0F;
        }
        LocalPlayer player = activePlayer();
        if (player == null) {
            return 0.0F;
        }
        return PlatformServices.gameplay().movementState(player).getContext()
                .getRenderFovBonus(partialTick());
    }

    public static void transformHands(PoseStack poseStack, float partialTick) {
        LocalPlayer player = activePlayer();
        if (player == null) {
            return;
        }
        var context = PlatformServices.gameplay().movementState(player).getContext();
        poseStack.translate(0.0, context.getRenderArmOffsetY(partialTick), 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(context.getRenderArmRotX(partialTick)));
    }

    private static LocalPlayer activePlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !PlatformServices.gameplay().hasMovementState(player)) {
            return null;
        }
        if (PlatformServices.gameplay().movementState(player).getCurrentState().getStateType()
                == StateType.ORIGINAL) {
            return null;
        }
        return player;
    }

    private static float partialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}
