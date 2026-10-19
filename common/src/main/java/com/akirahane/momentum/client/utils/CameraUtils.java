package com.akirahane.momentum.client.utils;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CameraUtils {
    public static void tickCameraRoll(PlayerMomentumContext context) {
        context.setPrevCameraRoll(context.getCurrentCameraRoll());

        // 回正速度快于倾斜速度。
        float smoothing = Math.abs(context.getTargetCameraRoll()) > Math.abs(context.getCurrentCameraRoll()) ? 0.24F : 0.5F;

        float diff = context.getTargetCameraRoll() - context.getCurrentCameraRoll();
        context.setCurrentCameraRoll(context.getCurrentCameraRoll() + diff * smoothing);

        if (Math.abs(context.getCurrentCameraRoll() - context.getTargetCameraRoll()) < 0.01F) {
            context.setCurrentCameraRoll(context.getTargetCameraRoll());
        }
    }

    public static void tickFovBonus(PlayerMomentumContext context) {
        context.setPrevFovBonus(context.getCurrentFovBonus());
        float diff = context.getTargetFovBonus() - context.getCurrentFovBonus();
        context.setCurrentFovBonus(context.getCurrentFovBonus() + diff * 0.15F);
    }

    public static void computeTargetFovBonus(Player player, PlayerMomentumContext context) {
        MomentumStateMachine sm = player.getData(InitAttachments.MOMENTUM_STATE);
        if (sm.getCurrentState().getStateType() == StateType.ORIGINAL) {
            context.setTargetFovBonus(0F);
            return;
        }
        if (!ClientConfig.ENABLE_CAMERA_OFFSET.get()) {
            context.setTargetFovBonus(0F);
            return;
        }
        double speedPerSec = context.getSpeed().horizontalDistance() * 20;
        double minSpeed = 5.0;
        double maxSpeed = 21.0;
        float speedFactor = (float) Mth.clamp((speedPerSec - minSpeed) / (maxSpeed - minSpeed), 0.0, 1.0);
        // 遵循原版“视场角效果”选项。
        float fovEffectScale = Minecraft.getInstance().options.fovEffectScale().get().floatValue();
        context.setTargetFovBonus(speedFactor * (float) ClientConfig.FOV_BONUS_MAX.get().doubleValue() * fovEffectScale);
    }

    public static void tickMomentumRoll(Player player, PlayerMomentumContext context) {
        context.setPrevMomentumRoll(context.getCurrentMomentumRoll());

        float targetRoll = 0F;

        if (context.getMomentumRollIntensity() != 0F) {
            Vec3 velocity = player.getDeltaMovement();
            double horizontalSpeed = velocity.horizontalDistance();

            double speedPerSec = horizontalSpeed * 20;
            double minSpeed = 5.0;
            double maxSpeed = 21.0;

            if (speedPerSec > minSpeed) {
                Vec3 vDir = new Vec3(velocity.x, 0, velocity.z).normalize();
                float yaw = player.getYRot();
                Vec3 lookDir = new Vec3(
                        -Math.sin(Math.toRadians(yaw)),
                        0,
                        Math.cos(Math.toRadians(yaw))
                );

                // 叉积 y = sin(夹角)，范围 [-1, 1]
                double crossY = vDir.x * lookDir.z - vDir.z * lookDir.x;

                // sin(15°)，忽略小角度偏差。
                double angleThreshold = 0.26;

                if (Math.abs(crossY) > angleThreshold) {
                    // 从阈值起连续增加倾斜，避免突变。
                    double sign = Math.signum(crossY);
                    double mappedCross = (Math.abs(crossY) - angleThreshold) / (1.0 - angleThreshold);
                    mappedCross = Math.min(1.0, mappedCross) * sign;

                    float speedFactor = (float) Mth.clamp(
                            (speedPerSec - minSpeed) / (maxSpeed - minSpeed),
                            0.0, 1.0
                    );

                    targetRoll = (float) mappedCross * context.getMomentumRollIntensity() * speedFactor;
                }
            }
        }

        float smoothing = 0.2F;
        context.setCurrentMomentumRoll(Mth.lerp(smoothing, context.getCurrentMomentumRoll(), targetRoll));

        if (Math.abs(context.getCurrentMomentumRoll()) < 0.01F && targetRoll == 0F) {
            context.setCurrentMomentumRoll(0F);
        }
    }
}
