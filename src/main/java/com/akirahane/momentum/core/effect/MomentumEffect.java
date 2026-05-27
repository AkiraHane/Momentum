package com.akirahane.momentum.core.effect;


import lombok.Getter;
import lombok.Setter;

// 临时变量
@Getter
@Setter
public class MomentumEffect {
    // 数值
    private float value;
    // 每Tick变化数值
    private float modifyValue;
    // 倍率
    private float multiplier;
    // 每Tick变化倍率
    private float modifyMultiplier;
    // 持续Tick
    private int duration;

    public MomentumEffect() {
        this.init();
    }

    public void init() {
        this.value = 0;
        this.modifyValue = 0;
        this.multiplier = 1;
        this.modifyMultiplier = 0;
        this.duration = 0;
    }

    public MomentumEffect(float value, float modifyValue, float multiplier, float modifyMultiplier, int duration) {
        this.value = value;
        this.modifyValue = modifyValue;
        this.multiplier = multiplier;
        this.modifyMultiplier = modifyMultiplier;
        this.duration = duration;
    }
}
