package com.akirahane.momentum.config;

import net.neoforged.neoforge.common.ModConfigSpec;

// 一个示例配置类。这不是必需的，但拥有一个配置类来保持配置有序是个好主意。
// 演示了如何使用 Neo 的配置 API
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 功能配置
    static {
        BUILDER.push("function_settings");
    }
    public static final ModConfigSpec.BooleanValue ALLOW_MANEUVER_WITHOUT_BOOSTER = BUILDER
            .comment("是否允许在不使用助推器的情况下进行机动操作")
            .define("allowManeuverWithoutThruster", true);

    public static final ModConfigSpec.BooleanValue MANEUVER_CONSUME_HUNGER = BUILDER
            .comment("机动操作是否额外消耗饥饿值")
            .define("maneuverConsumeHunger", false);

    // 新玩家是否默认开启机动
    public static final ModConfigSpec.BooleanValue DEFAULT_ENABLE_MANEUVER = BUILDER
            .comment("新玩家是否默认开启机动")
            .define("defaultEnableManeuver", true);

    // 机动操作每tick消耗的饥饿值
    public static final ModConfigSpec.DoubleValue MANEUVER_CONSUME_HUNGER_AMOUNT = BUILDER
            .comment(" Jed: 0.0 = 无消耗，1.0 = 完全消耗 默认值: 0.01")
            .defineInRange("maneuverConsumeHungerAmount", 0.01, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue BOOSTER_STAMINA_REDUCTION = BUILDER
            .comment("装备助推器时体力消耗的减免幅度", "0.0 = 无减免，1.0 = 完全减免")
            .defineInRange("boosterStaminaReduction", 1.0, 0.0, 1.0);

    // ========== 机动配置 ==========
    public static final ModConfigSpec.DoubleValue MIN_SLIDE_SPEED = BUILDER
            .comment("滑铲最低速度（米/秒）", "默认值: 4.0")
            .defineInRange("minSlideSpeed", 4.0, 1.0, 100.0);

    // 墙跑最低速度
    public static final ModConfigSpec.DoubleValue MIN_WALL_RUN_SPEED = BUILDER
            .comment("墙跑最低速度（米/秒）", "默认值: 4.0")
            .defineInRange("minWallRunSpeed", 4.0, 1.0, 100.0);

    // 水上漂最低速度
    public static final ModConfigSpec.DoubleValue MIN_WATER_SWIM_SPEED = BUILDER
            .comment("水上漂最低速度（米/秒）", "默认值: 10.0")
            .defineInRange("minWaterSwimSpeed", 10.0, 1.0, 100.0);

    // 滑铲加速冷却
    public static final ModConfigSpec.IntValue SLIDE_ACCELERATION_COOLDOWN = BUILDER
            .comment("滑铲加速冷却（tick）", "默认值: 40")
            .defineInRange("slideAccelerationCooldown", 40, 0, 200);

    // 单次闪避冷却
    public static final ModConfigSpec.IntValue DODGE_COOLDOWN = BUILDER
            .comment("单次闪避冷却（tick）", "默认值: 40")
            .defineInRange("airDodgeCooldown", 40, 0, 200);

    // 闪避可储存次数
    public static final ModConfigSpec.IntValue DODGE_STORAGE = BUILDER
            .comment("闪避可储存次数", "默认值: 2")
            .defineInRange("airDodgeStorage", 2, 0, 10);

    // 单次水中冲刺冷却
    public static final ModConfigSpec.IntValue WATER_SPRINT_COOLDOWN = BUILDER
            .comment("水中冲刺冷却（tick）", "默认值: 40")
            .defineInRange("waterSprintCooldown", 40, 0, 200);

    // 水上冲刺可储存次数
    public static final ModConfigSpec.IntValue WATER_SPRINT_STORAGE = BUILDER
            .comment("水上冲刺可储存次数", "默认值: 2")
            .defineInRange("waterSprintStorage", 2, 0, 10);

    // 空气阻力
    public static final ModConfigSpec.DoubleValue AIR_FRICTION = BUILDER
            .comment("空气阻力（0.0 - 1.0）", "默认值: 0.998, 原版: 0.91")
            .defineInRange("airFriction", 0.998, 0.0, 1.0);

    // 爬梯加速倍率
    public static final ModConfigSpec.DoubleValue CLIMB_BOOST_MULTIPLIER = BUILDER
            .comment("爬梯加速倍率", "默认值: 3.0")
            .defineInRange("climbBoostMultiplier", 3.0, 0.0, 10.0);

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

    public static final ModConfigSpec SPEC = BUILDER.build();
}