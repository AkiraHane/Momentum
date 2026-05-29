package com.akirahane.momentum.core.effect;

import lombok.Data;

@Data
public class MomentumEffect {
    // 数值
    private float value;
    // 倍率
    private float multiplier;

    public MomentumEffect() {
        this.init();
    }

    public void init() {
        this.value = 0;
        this.multiplier = 1.0f;
    }
}
