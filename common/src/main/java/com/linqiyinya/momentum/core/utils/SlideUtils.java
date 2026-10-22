package com.linqiyinya.momentum.core.utils;

import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.core.state.StateType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.linqiyinya.momentum.core.effect.MomentumEffectType.ACCELERATION;

public class SlideUtils {
    // 滑行最大速度上限（格/tick）
    public static final double MAX_SLIDE_SPEED = 0.98;
    // 下坡基础加速系数
    public static final float DOWNHILL_ACCEL_FACTOR = 0.08F;
    // 上坡减速系数
    public static final float UPHILL_DECEL_FACTOR = 0.12F;

    public static void setSlideAcceleration(Vec3 movement, double movementStepY, MomentumStateMachine stateMachine, Player player) {
        if (!stateMachine.getCurrentState().getStateType().equals(StateType.SLIDE)) {
            return;
        }

        double currentSpeed = movement.horizontalDistance();
        stateMachine.getContext().setBlockStep((movementStepY + stateMachine.getContext().getBlockStep()) / 2);

        int duration = 2; // 当前 tick 与下一 tick

        Vec3 slopeDir = stateMachine.getContext().getSlopeUnitVector();
        if (movementStepY < 0) {
            double dropHeight = Math.abs(movementStepY);

            // 速度越接近上限，剩余加速空间越小。
            double speedRatio = Math.min(1.0, currentSpeed / MAX_SLIDE_SPEED);
            double headroom = 1.0 - speedRatio * speedRatio;

            // 速度与落差越接近，该倍率越大。
            double matchRatio = Math.min(currentSpeed, dropHeight) / Math.max(Math.max(currentSpeed, dropHeight), 0.001);
            float acceleration = (float) (DOWNHILL_ACCEL_FACTOR * dropHeight * 3 * headroom * matchRatio);
            stateMachine.getContext().SLIDE_ACCELERATION.setValue(new Vec3(slopeDir.x * acceleration, 0, slopeDir.z * acceleration));
            stateMachine.getContext().addEffect(ACCELERATION, stateMachine.getContext().SLIDE_ACCELERATION, duration);

        } else if (movementStepY > 0) {
            double riseHeight = Math.abs(movementStepY);

            float deceleration = (float) (UPHILL_DECEL_FACTOR * Math.min(riseHeight * riseHeight, 1.0));

            stateMachine.getContext().SLIDE_ACCELERATION.setValue(new Vec3(
                    slopeDir.x * deceleration,
                    0,
                    slopeDir.z * deceleration));
            stateMachine.getContext().addEffect(ACCELERATION, stateMachine.getContext().SLIDE_ACCELERATION, duration);
        }
    }
}
