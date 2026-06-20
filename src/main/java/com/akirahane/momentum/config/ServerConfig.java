package com.akirahane.momentum.config;

import net.neoforged.neoforge.common.ModConfigSpec;

// 一个示例配置类。这不是必需的，但拥有一个配置类来保持配置有序是个好主意。
// 演示了如何使用 Neo 的配置 API
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ALLOW_MANEUVER_WITHOUT_BOOSTER = BUILDER
            .comment("是否允许在不使用助推器的情况下进行机动操作")
            .define("allowManeuverWithoutThruster", true);

    public static final ModConfigSpec.BooleanValue MANEUVER_CONSUME_HUNGER = BUILDER
            .comment("机动操作是否额外消耗饥饿值")
            .define("maneuverConsumeHunger", true);

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

    // 滑铲加速冷却
    public static final ModConfigSpec.IntValue SLIDE_ACCELERATION_COOLDOWN = BUILDER
            .comment("滑铲加速冷却（tick）", "默认值: 40")
            .defineInRange("slideAccelerationCooldown", 40, 0, 200);

    // 单次闪避冷却
    public static final ModConfigSpec.IntValue DODGE_COOLDOWN = BUILDER
            .comment("单次闪避冷却（tick）", "默认值: 20")
            .defineInRange("airDodgeCooldown", 20, 0, 200);

    // 闪避可储存次数
    public static final ModConfigSpec.IntValue DODGE_STORAGE = BUILDER
            .comment("闪避可储存次数", "默认值: 2")
            .defineInRange("airDodgeStorage", 2, 0, 10);

    // 空气阻力
    public static final ModConfigSpec.DoubleValue AIR_FRICTION = BUILDER
            .comment("空气阻力（0.0 - 1.0）", "默认值: 0.998, 原版: 0.91")
            .defineInRange("airFriction", 0.998, 0.0, 1.0);
    public static final ModConfigSpec SPEC = BUILDER.build();
}