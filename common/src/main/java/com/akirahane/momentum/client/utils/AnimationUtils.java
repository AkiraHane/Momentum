package com.akirahane.momentum.client.utils;

import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.client.animation.MomentumAnimationRegistration;
import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

public class AnimationUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void initialize(Player player, PlayerMomentumContext context) {
        context.setController((MomentumAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                player, MomentumAnimationRegistration.MOMENTUM_ANIM
        ));
        if (context.getController() != null) {
            context.setMocha(context.getController().getMolangRuntime());
            bindVariables(context);
        }
    }

    private static void bindVariables(PlayerMomentumContext context) {
        Value value = context.getMocha().scope().get("math");
        if (value instanceof ObjectValue math) {
            math.setFunction("clamp", Mth::clamp);
            math.setFunction("min", Math::min);
            math.setFunction("atan2", (y, x) -> (float) Math.toDegrees(Mth.atan2(y, x)));
        } else {
            LOGGER.warn("Failed to bind math.min_angle_180 to Mocha");
        }
        value = context.getMocha().scope().get("variable");
        if (value instanceof ObjectValue variable) {
            final Minecraft mc = Minecraft.getInstance();

            variable.setFunction("get_movement_speed", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, (float) context.getOldSpeed().horizontalDistance(),
                        (float) context.getSpeed().horizontalDistance());
            });
            variable.setFunction("get_movement_y_speed", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, (float) context.getOldSpeed().y, (float) context.getSpeed().y());
            });
            variable.setFunction("stable_body_head_angle", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, context.getPrevBodyHeadAngleDiff(), context.getBodyHeadAngleDiff());
            });
            variable.setFunction("smooth_head_x_rotation", () -> {
                if (context.getController() != null && context.getController().getAvatar() instanceof Player player) {
                    float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                    return Mth.lerp(pt, context.getPrevHeadXRot(), player.getXRot());
                }
                return 0F;
            });
            variable.setFunction("dolphin_tilt", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                Vec3 speed = context.getSpeed();
                Vec3 old = context.getOldSpeed();
                float h = Mth.lerp(pt, (float) old.horizontalDistance(), (float) speed.horizontalDistance());
                float vy = Mth.lerp(pt, (float) old.y, (float) speed.y);
                float thetaDeg = (float) Math.toDegrees(Math.atan2(vy, Math.max(h, 0.001)));
                return -thetaDeg;
            });
        } else {
            LOGGER.warn("Failed to bind variable.get_movement_speed to Mocha");
        }
    }

    public static void tickArmOffset(PlayerMomentumContext context) {
        context.setPrevArmOffsetY(context.getArmOffsetY());
        context.setPrevArmRotX(context.getArmRotX());

        context.setArmOffsetY(Mth.lerp(0.2F, context.getArmOffsetY(), context.getTargetArmOffsetY()));
        context.setArmRotX(Mth.lerp(0.2F, context.getArmRotX(), context.getTargetArmRotX()));
    }

    /**
     * 根据相邻 tick 的速度仰角变化计算动画播放速度。
     * @return 播放速度倍率，范围 [0.4, 2.9]
     */
    public static float computeAnimSpeedFromVelocityAngle(Vec3 curSpeed, Vec3 prevSpeed) {
        float hCur = (float) curSpeed.horizontalDistance();
        float hPrev = (float) prevSpeed.horizontalDistance();
        double thetaCur = Math.atan2(curSpeed.y, Math.max(hCur, 0.001));
        double thetaPrev = Math.atan2(prevSpeed.y, Math.max(hPrev, 0.001));
        double deltaDeg = Math.toDegrees(Math.abs(thetaCur - thetaPrev));
        return (float) (0.4 + Math.clamp(deltaDeg * 0.14, 0.0, 2.5));
    }

    /**
     * 用速度方向变化和位置误差，使动画进度跟随速度仰角。
     * 时间参数以秒计，角度以度计；angleTop/angleBottom 对应动画起点/终点。
     * @param kP 动画位置误差增益
     * @param kF 速度仰角变化率增益
     * @return 播放速度倍率，范围 [0, 5]
     */
    public static float computeAnimSpeedByAngleTracking(
            Vec3 velocity, Vec3 prevVelocity, float animTickSec, float animLengthSec,
            float angleTop, float angleBottom, float kP, float kF) {

        float h = (float) velocity.horizontalDistance();
        float thetaDeg = (float) Math.toDegrees(Math.atan2(velocity.y * 0.2F, Math.max(h, 0.001)));
        float thetaClamped = Mth.clamp(thetaDeg, angleBottom, angleTop);

        float hPrev = (float) prevVelocity.horizontalDistance();
        float thetaPrevDeg = (float) Math.toDegrees(Math.atan2(prevVelocity.y * 0.2F, Math.max(hPrev, 0.001)));
        float thetaPrevClamped = Mth.clamp(thetaPrevDeg, angleBottom, angleTop);

        float range = angleTop - angleBottom;
        if (range <= 1e-6f) return 1.0f;

        float fraction = (angleTop - thetaClamped) / range;
        float targetPos = fraction * animLengthSec;

        // 正值表示向动画终点移动。
        float angleDelta = thetaPrevClamped - thetaClamped;
        float feedForward = (angleDelta / range) * animLengthSec * 20.0f * kF;

        float error = targetPos - animTickSec;
        float pCorrection = kP * error;

        float speed = feedForward + pCorrection;
        return Mth.clamp(speed, 0.0f, 5.0f);
    }
}
