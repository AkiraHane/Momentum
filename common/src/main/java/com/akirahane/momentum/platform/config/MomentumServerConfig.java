package com.akirahane.momentum.platform.config;

/** Loader-neutral server gameplay configuration. */
public final class MomentumServerConfig {
    public static final ConfigValue<Boolean> ALLOW_MANEUVER_WITHOUT_BOOSTER = value(true);
    public static final ConfigValue<Boolean> MANEUVER_CONSUME_HUNGER = value(false);
    public static final ConfigValue<Boolean> DEFAULT_ENABLE_MANEUVER = value(true);
    public static final ConfigValue<Double> MANEUVER_CONSUME_HUNGER_AMOUNT = value(0.01D);
    public static final ConfigValue<Double> BOOSTER_STAMINA_REDUCTION = value(1.0D);
    public static final ConfigValue<Double> MIN_SLIDE_SPEED = value(4.0D);
    public static final ConfigValue<Double> MIN_WALL_RUN_SPEED = value(4.0D);
    public static final ConfigValue<Double> MIN_WATER_SWIM_SPEED = value(10.0D);
    public static final ConfigValue<Integer> SLIDE_ACCELERATION_COOLDOWN = value(20);
    public static final ConfigValue<Integer> DODGE_COOLDOWN = value(40);
    public static final ConfigValue<Integer> DODGE_STORAGE = value(2);
    public static final ConfigValue<Double> AIR_FRICTION = value(0.998D);
    public static final ConfigValue<Double> AIR_ACCELERATION_XZ = value(1.0D);
    public static final ConfigValue<Double> CLIMB_BOOST_MULTIPLIER = value(3.0D);
    public static final ConfigValue<Integer> WALL_KICK_ACCELERATION_COOLDOWN = value(10);

    public static final ConfigValue<Boolean> ENABLE_PRONE = value(true);
    public static final ConfigValue<Boolean> ENABLE_SLIDE = value(true);
    public static final ConfigValue<Boolean> ENABLE_BREAK_FALL_READY = value(true);
    public static final ConfigValue<Boolean> ENABLE_BREAK_FALL = value(true);
    public static final ConfigValue<Boolean> ENABLE_DODGE = value(true);
    public static final ConfigValue<Boolean> ENABLE_WALL_CLIMB = value(true);
    public static final ConfigValue<Boolean> ENABLE_WALL_SLIDE = value(true);
    public static final ConfigValue<Boolean> ENABLE_WALL_RUN = value(true);
    public static final ConfigValue<Boolean> ENABLE_WALL_HANG = value(true);
    public static final ConfigValue<Boolean> ENABLE_POWER_JUMP = value(true);
    public static final ConfigValue<Boolean> ENABLE_WALL_KICK = value(true);
    public static final ConfigValue<Boolean> ENABLE_VAULT_UP = value(true);
    public static final ConfigValue<Boolean> ENABLE_VAULT_IN = value(true);
    public static final ConfigValue<Boolean> ENABLE_WATER_RUN = value(true);
    public static final ConfigValue<Boolean> ENABLE_WATER_PUSH = value(true);
    public static final ConfigValue<Boolean> ENABLE_FALL_SLOW = value(true);
    public static final ConfigValue<Boolean> ENABLE_AIR_JUMP = value(true);

    private MomentumServerConfig() {
    }

    private static <T> ConfigValue<T> value(T defaultValue) {
        return new ConfigValue<>(defaultValue);
    }
}
