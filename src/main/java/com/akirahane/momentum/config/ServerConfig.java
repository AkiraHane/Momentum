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
            .define("maneuverConsumeHunger", false);

    public static final ModConfigSpec.DoubleValue BOOSTER_STAMINA_REDUCTION = BUILDER
            .comment("装备助推器时体力消耗的减免幅度", "0.0 = 无减免，1.0 = 完全减免")
            .defineInRange("boosterStaminaReduction", 1.0, 0.0, 1.0);

    // 自动上坡高度
    public static final ModConfigSpec.DoubleValue ADD_AUTO_CLIMB_HEIGHT = BUILDER
            .comment("机动模式下增加的自动上坡高度（米），设为0则为不额外增加自动上坡高度，修改需要重启服务", "默认值: 0.5")
            .defineInRange("autoClimbHeight", 0.5, 0.0, 0.6);

    // ========== 体力系统配置 ==========

    public static final ModConfigSpec.BooleanValue MANEUVER_CONSUMES_STAMINA = BUILDER
            .comment("机动行为是否消耗体力", "默认值: true")
            .define("maneuverConsumesStamina", true);

    public static final ModConfigSpec.IntValue STAMINA_MAX_VALUE = BUILDER
            .comment("体力最大值", "默认值: 100")
            .defineInRange("staminaMaxValue", 100, 1, 1000);

    public static final ModConfigSpec.DoubleValue STAMINA_REGEN_PER_SECOND = BUILDER
            .comment("体力自然恢复速度（每秒恢复点数）", "默认值: 5.0")
            .defineInRange("staminaRegenPerSecond", 5.0, 0.0, 50.0);

    public static final ModConfigSpec.DoubleValue STAMINA_REGEN_DELAY = BUILDER
            .comment("体力恢复延迟（秒，停止消耗后多久开始恢复）", "默认值: 2.0")
            .defineInRange("staminaRegenDelay", 2.0, 0.0, 10.0);

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

    // 空气阻力
    public static final ModConfigSpec.DoubleValue AIR_FRICTION = BUILDER
            .comment("空气阻力（0.0 - 1.0）", "默认值: 0.998, 原版: 0.91")
            .defineInRange("airFriction", 0.998, 0.0, 1.0);
    public static final ModConfigSpec SPEC = BUILDER.build();
}