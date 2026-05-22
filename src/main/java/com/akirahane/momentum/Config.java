package com.akirahane.momentum;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

// 一个示例配置类。这不是必需的，但拥有一个配置类来保持配置有序是个好主意。
// 演示了如何使用 Neo 的配置 API
public class Config {
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

    public static final ModConfigSpec.DoubleValue STAMINA_LOW_WARNING_THRESHOLD = BUILDER
            .comment("体力低于此百分比时进行警告", "范围: 0.0 - 1.0", "默认值: 0.2 (20%)")
            .defineInRange("staminaLowWarningThreshold", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue SHOW_STAMINA_BAR = BUILDER
            .comment("是否显示体力条", "默认值: true")
            .define("showStaminaBar", true);

    public static final ModConfigSpec.IntValue STAMINA_BAR_POSITION = BUILDER
            .comment("体力条的渲染位置", "0=左上, 1=右上, 2=左下, 3=右下", "默认值: 1")
            .defineInRange("staminaBarPosition", 1, 0, 3);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(Identifier.parse(itemName));
    }
}