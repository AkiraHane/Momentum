package com.akirahane.momentum.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    // 按键提示
    static {
        BUILDER.push("key_hints");
    }
    public static final ModConfigSpec.DoubleValue MIN_ALPHA_WHEN_MOVING = BUILDER
            .comment("移动时最低透明度", "范围: 0.0 - 1.0", "默认值: 0.2")
            .defineInRange("minAlphaWhenMoving", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_ALPHA = BUILDER
            .comment("最大透明度", "范围: 0.0 - 1.0", "默认值: 1.0")
            .defineInRange("maxAlpha", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.IntValue FRESH_DURATION = BUILDER
            .comment("内容变化后高亮持续时间（单位：tick）", "默认值: 80 (4秒)")
            .defineInRange("freshDuration", 80, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue IDLE_DELAY = BUILDER
            .comment("静止多久后淡入（单位：tick）", "默认值: 30 (1.5秒)")
            .defineInRange("idleDelay", 30, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MOVE_THRESHOLD_SQR = BUILDER
            .comment("视为移动的速度阈值（平方值）", "默认值: 0.005")
            .defineInRange("moveThresholdSqr", 0.005, 0.0, Double.MAX_VALUE);

    // 缓动速度（值越小越慢）
    public static final ModConfigSpec.DoubleValue FADE_IN_SPEED = BUILDER
            .comment("淡入速度（值越小越慢）", "范围: 0.01 - 1.0", "默认值: 0.08")
            .defineInRange("fadeInSpeed", 0.08, 0.01, 1.0);

    public static final ModConfigSpec.DoubleValue FADE_OUT_SPEED = BUILDER
            .comment("淡出速度（值越小越慢）", "范围: 0.01 - 1.0", "默认值: 0.18")
            .defineInRange("fadeOutSpeed", 0.18, 0.01, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}