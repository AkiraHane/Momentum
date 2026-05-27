package com.akirahane.momentum.core.common;

import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.movements.grounds.SlideState;
import com.akirahane.momentum.core.init.InitAttachments;
import com.akirahane.momentum.server.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import static com.akirahane.momentum.core.common.effect.MomentumEffectType.ACCELERATION;
import static com.akirahane.momentum.core.common.effect.MomentumEffectType.BLOCK_FRICTION_MULTIPLIER;

public class MomentumUtils {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    public static float getAirFriction(Player player) {
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return 0.91F;
        }
        // =================== 内容 ===================
        return ServerConfig.AIR_FRICTION.get().floatValue();
    }

    public static Vec3 getClearVec3(Vec3 original) {
        return new Vec3(
                Mth.equal(original.x, 0.0) ? 0.0 : original.x,
                Mth.equal(original.y, 0.0) ? 0.0 : original.y,
                Mth.equal(original.z, 0.0) ? 0.0 : original.z
        );
    }

    // 滑行上下坡加速和减速
    public static void setSlideAcceleration(Vec3 movement, double movementStepY, MovementStateMachine stateMachine) {

        if (!stateMachine.getCurrentState().getStateType().equals(StateType.SLIDE)) {
            return;
        }


        double currentSpeed = movement.horizontalDistance();
        stateMachine.getContext().setBlockStep(movementStepY);

        // === 核心参数 ===
        // 滑行最大速度上限（格/tick），约等于 sprint 速度的 1.5 倍
        // Minecraft sprint ≈ 0.28 blocks/tick, 上限设为 0.42
        final double MAX_SLIDE_SPEED = 0.98;
        // 下坡基础加速系数
        final float DOWNHILL_ACCEL_FACTOR = 0.08F;
        // 上坡减速系数
        final float UPHILL_DECEL_FACTOR = 0.12F;
        // 下坡摩擦力降低的最小值（不会低于此值，防止无摩擦滑行）
        final float MIN_FRICTION_MULTIPLIER = 0F;

        // duration 固定为 1 tick，因为每次阶梯变化都会重新调用
        // 避免低速时 duration 过长导致效果堆积
        int duration = 2; // 1 + 1 用于下一 tick 处理

        // === 下坡（movementStepY < 0）===
        if (movementStepY < 0) {
            double dropHeight = Math.abs(movementStepY);

            // 速度越接近上限，加速越小（渐近式衰减）
            // speedRatio: 0 → 刚起步，1 → 已达上限
            double speedRatio = Math.min(1.0, currentSpeed / MAX_SLIDE_SPEED);
            // 剩余加速空间，越快加速越少
            double headroom = 1.0 - speedRatio * speedRatio;

            // 加速值 = 落差产生的势能转化 × 剩余空间
            // dropHeight 通过 tanh 软限制，防止大落差产生过大加速
            float acceleration = (float) (DOWNHILL_ACCEL_FACTOR * Math.tanh(dropHeight * 2.0) * headroom);

            // 摩擦力倍率：下坡时略微降低摩擦（更滑），但有下限
            float frictionMultiplier = (float) Math.max(
                    MIN_FRICTION_MULTIPLIER,
                    1.0 - (1.0 - MIN_FRICTION_MULTIPLIER) * dropHeight * 2.0
            );

            SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setDuration(duration);
            SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setMultiplier(frictionMultiplier);
            stateMachine.getContext().getPendingEffectPool().get(BLOCK_FRICTION_MULTIPLIER).add(
                    SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER
            );

            SlideState.TEMP_ACCELERATION.setDuration(duration);
            SlideState.TEMP_ACCELERATION.setValue(acceleration);
            stateMachine.getContext().getPendingEffectPool().get(ACCELERATION).add(
                    SlideState.TEMP_ACCELERATION
            );

            // === 上坡（movementStepY > 0）===
        } else if (movementStepY > 0) {
            double riseHeight = Math.abs(movementStepY);

            // 上坡减速：与坡度成正比，但用 sqrt 软化手感
            // 不会一步减到 0，保留滑行惯性感
            float deceleration = (float) (-UPHILL_DECEL_FACTOR * Math.min(riseHeight * riseHeight, 1.0));

            // 上坡时增加摩擦（加速衰减）
            float frictionMultiplier = (float) (1.0 + 0.15 * Math.sqrt(riseHeight));
            frictionMultiplier = Math.min(frictionMultiplier, 1.3F);

            SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setDuration(duration);
            SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setMultiplier(frictionMultiplier);
            stateMachine.getContext().getPendingEffectPool().get(BLOCK_FRICTION_MULTIPLIER).add(
                    SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER
            );

            SlideState.TEMP_ACCELERATION.setDuration(duration);
            SlideState.TEMP_ACCELERATION.setValue(deceleration);
            stateMachine.getContext().getPendingEffectPool().get(ACCELERATION).add(
                    SlideState.TEMP_ACCELERATION
            );
        }
    }
}
