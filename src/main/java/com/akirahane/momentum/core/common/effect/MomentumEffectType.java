package com.akirahane.momentum.core.common.effect;

public enum MomentumEffectType {
    // 临时加速
    ACCELERATION,
    // 临时摩擦力
    FRICTION,
    // 滑铲冷却
    SLIDE_COOLDOWN,
    // 加速限速
    ACCELERATION_LIMIT_SPEED,
    // 上一个tickMixin同步过来的滑铲方块摩擦力倍率
    BLOCK_FRICTION_MULTIPLIER,
    // 其他
    OTHER
}
