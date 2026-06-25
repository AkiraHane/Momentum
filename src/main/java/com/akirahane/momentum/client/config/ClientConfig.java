package com.akirahane.momentum.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    // 是否开启摄像头偏移
    public static final ModConfigSpec.BooleanValue ENABLE_CAMERA_OFFSET = BUILDER
            .comment("是否开启摄像头偏移")
            .define("enableCameraOffset", true);

    // 是否使用双击疾跑闪避(防止误操作)
    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_CLICK_DODGE = BUILDER
            .comment("是否使用双击疾跑闪避(防止误操作)")
            .define("enableDoubleClickDodge", true);
    // 按键提示
    static {
        BUILDER.push("key_hints");
    }
    // 默认是否开启按键提示
    public static final ModConfigSpec.BooleanValue ENABLE_KEY_HINTS = BUILDER
            .comment("是否显示按键教程")
            .define("enableKeyHints", true);
    public static final ModConfigSpec.DoubleValue MIN_ALPHA_WHEN_MOVING = BUILDER
            .comment("移动时最低透明度", "范围: 0.0 - 1.0", "默认值: 0.4")
            .defineInRange("minAlphaWhenMoving", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_ALPHA = BUILDER
            .comment("最大透明度", "范围: 0.0 - 1.0", "默认值: 1.0")
            .defineInRange("maxAlpha", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.IntValue FRESH_DURATION = BUILDER
            .comment("内容变化后高亮持续时间（单位：tick）", "默认值: 80 (4秒)")
            .defineInRange("freshDuration", 80, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue IDLE_DELAY = BUILDER
            .comment("静止多久后淡入（单位：tick）", "默认值: 100 (5秒)")
            .defineInRange("idleDelay", 100, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue MOVE_THRESHOLD_SQR = BUILDER
            .comment("视为移动的速度阈值（平方值）", "默认值: 0.005")
            .defineInRange("moveThresholdSqr", 0.005, 0.0, Double.MAX_VALUE);

    // 缓动速度（值越小越慢）
    public static final ModConfigSpec.DoubleValue FADE_IN_SPEED = BUILDER
            .comment("淡入速度（值越小越慢）", "范围: 0 - 1.0", "默认值: 0.08")
            .defineInRange("fadeInSpeed", 0.01, 0, 1.0);

    public static final ModConfigSpec.DoubleValue FADE_OUT_SPEED = BUILDER
            .comment("淡出速度（值越小越慢）", "范围: 0 - 1.0", "默认值: 0.18")
            .defineInRange("fadeOutSpeed", 0.18, 0, 1.0);
    // 功能开关
    static {
        BUILDER.pop();
        BUILDER.push("function_switches");
    }
    public static final ModConfigSpec.BooleanValue ENABLE_PRONE = BUILDER
            .comment("是否开启趴下")
            .define("enableProne", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SLIDE = BUILDER
            .comment("是否开启滑铲")
            .define("enableSlide", true);
    public static final ModConfigSpec.BooleanValue ENABLE_BREAK_FALL_READY = BUILDER
            .comment("是否开启受身准备")
            .define("enableBreakFallReady", true);
    public static final ModConfigSpec.BooleanValue ENABLE_BREAK_FALL = BUILDER
            .comment("是否开启受身")
            .define("enableBreakFall", true);
    public static final ModConfigSpec.BooleanValue ENABLE_DODGE = BUILDER
            .comment("是否开启闪避")
            .define("enableDodge", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_CLIMB = BUILDER
            .comment("是否开启爬墙")
            .define("enableWallClimb", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_SLIDE = BUILDER
            .comment("是否开启滑墙")
            .define("enableWallSlide", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_RUN = BUILDER
            .comment("是否开启墙跑")
            .define("enableWallRun", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_HANG = BUILDER
            .comment("是否开启挂墙")
            .define("enableWallHang", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WALL_KICK = BUILDER
            .comment("是否开启蹬墙跳")
            .define("enableWallKick", true);
    public static final ModConfigSpec.BooleanValue ENABLE_VAULT_UP = BUILDER
            .comment("是否开启翻越")
            .define("enableVaultUp", true);
    public static final ModConfigSpec.BooleanValue ENABLE_VAULT_IN = BUILDER
            .comment("是否开启翻入")
            .define("enableVaultIn", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WATER_RUN = BUILDER
            .comment("是否开启水上漂(需要助推器)")
            .define("enableWaterRun", true);
    public static final ModConfigSpec.BooleanValue ENABLE_WATER_PUSH = BUILDER
            .comment("是否开启水中推进(海豚跳)")
            .define("enableWaterPush", true);
    public static final ModConfigSpec.BooleanValue ENABLE_FALL_SLOW = BUILDER
            .comment("是否开启缓降(需要助推器)")
            .define("enableFallSlow", true);
    public static final ModConfigSpec.BooleanValue ENABLE_AIR_JUMP = BUILDER
            .comment("是否开启空中二段跳(需要助推器)")
            .define("enableAirJump", true);


    public static final ModConfigSpec SPEC = BUILDER.build();
}