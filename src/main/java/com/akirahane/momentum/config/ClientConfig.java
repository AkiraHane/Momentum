package com.akirahane.momentum.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.DoubleValue STAMINA_LOW_WARNING_THRESHOLD = BUILDER
            .comment("体力低于此百分比时进行警告", "范围: 0.0 - 1.0", "默认值: 0.2 (20%)")
            .defineInRange("staminaLowWarningThreshold", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue SHOW_STAMINA_BAR = BUILDER
            .comment("是否显示体力条", "默认值: true")
            .define("showStaminaBar", true);

    public static final ModConfigSpec.IntValue STAMINA_BAR_POSITION = BUILDER
            .comment("体力条的渲染位置", "0=左上, 1=右上, 2=左下, 3=右下", "默认值: 1")
            .defineInRange("staminaBarPosition", 1, 0, 3);

    public static final ModConfigSpec SPEC = BUILDER.build();
}