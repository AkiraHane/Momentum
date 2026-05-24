package com.akirahane.momentum.core.common.physics;

import net.minecraft.world.phys.Vec3;

public class FrictionHelper {

    /**
     * 混合摩擦模型
     *
     * @param currentSpeed 当前水平速度
     * @param friction     摩擦系数
     * @param stopSpeed    停止阈值
     * @return 摩擦后的速度保留比例
     */
    public static double calculateFriction(
            double currentSpeed,
            double friction,
            double stopSpeed) {

        if (currentSpeed < 0.001) return 0;

        // 低于停止阈值 → 直接停
        if (currentSpeed < stopSpeed) return 0;

        // 线性减速
        // drop = speed × friction × dt
        double drop = currentSpeed * friction;

        // 确保不会变成负数
        double newSpeed = Math.max(currentSpeed - drop, 0);

        // 返回比例（因为原版用的是乘法，要兼容）
        return newSpeed / currentSpeed;
    }

    /**
     * 应用摩擦到速度向量（只影响水平分量）
     */
    public static Vec3 applyFriction(Vec3 velocity, double friction,
                                     double stopSpeed) {
        double speed = velocity.horizontalDistance();
        double ratio = calculateFriction(speed, friction, stopSpeed);
        return new Vec3(
                velocity.x * ratio,
                velocity.y,
                velocity.z * ratio
        );
    }
}
