package com.akirahane.momentum.fabric.config;

import com.akirahane.momentum.platform.config.ConfigValue;
import com.akirahane.momentum.platform.config.MomentumServerConfig;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricServerConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("momentum-server.json");
    private static String synchronizedJson;

    private FabricServerConfig() {
    }

    public static void load() {
        JsonObject source = FabricConfigIo.read(PATH);
        if (source == null) {
            source = new JsonObject();
            apply(source);
            synchronizedJson = FabricConfigIo.toJson(normalized());
            return;
        }
        apply(source);
        JsonObject normalized = normalized();
        synchronizedJson = FabricConfigIo.toJson(normalized);
        FabricConfigIo.write(PATH, normalized);
    }

    public static void applySynchronized(String json) {
        JsonObject source = FabricConfigIo.parse(json);
        if (source != null) {
            apply(source);
        }
    }

    public static String synchronizedJson() {
        return synchronizedJson;
    }

    private static void apply(JsonObject root) {
        JsonObject settings = FabricConfigIo.child(root, "functionSettings");
        JsonObject switches = FabricConfigIo.child(root, "functionSwitches");

        setBool(settings, "allowManeuverWithoutThruster", MomentumServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER, true);
        setBool(settings, "maneuverConsumeHunger", MomentumServerConfig.MANEUVER_CONSUME_HUNGER, false);
        setBool(settings, "defaultEnableManeuver", MomentumServerConfig.DEFAULT_ENABLE_MANEUVER, true);
        setDouble(settings, "maneuverConsumeHungerAmount", MomentumServerConfig.MANEUVER_CONSUME_HUNGER_AMOUNT, 0.01, 0.0, 1.0);
        setDouble(settings, "boosterStaminaReduction", MomentumServerConfig.BOOSTER_STAMINA_REDUCTION, 1.0, 0.0, 1.0);
        setDouble(settings, "minSlideSpeed", MomentumServerConfig.MIN_SLIDE_SPEED, 4.0, 1.0, 100.0);
        setDouble(settings, "minWallRunSpeed", MomentumServerConfig.MIN_WALL_RUN_SPEED, 4.0, 1.0, 100.0);
        setDouble(settings, "minWaterSwimSpeed", MomentumServerConfig.MIN_WATER_SWIM_SPEED, 10.0, 1.0, 100.0);
        setInt(settings, "slideAccelerationCooldown", MomentumServerConfig.SLIDE_ACCELERATION_COOLDOWN, 20, 0, 200);
        setInt(settings, "airDodgeCooldown", MomentumServerConfig.DODGE_COOLDOWN, 40, 0, 200);
        setInt(settings, "airDodgeStorage", MomentumServerConfig.DODGE_STORAGE, 2, 0, 10);
        setDouble(settings, "airFriction", MomentumServerConfig.AIR_FRICTION, 0.998, 0.0, 1.0);
        setDouble(settings, "airAccelerationXz", MomentumServerConfig.AIR_ACCELERATION_XZ, 1.0, 0.0, 3.0);
        setDouble(settings, "climbBoostMultiplier", MomentumServerConfig.CLIMB_BOOST_MULTIPLIER, 3.0, 0.0, 10.0);
        setInt(settings, "wallKickAccelerationCooldown", MomentumServerConfig.WALL_KICK_ACCELERATION_COOLDOWN, 20, 0, 200);

        setSwitches(switches);
    }

    private static void setSwitches(JsonObject source) {
        setBool(source, "enableProne", MomentumServerConfig.ENABLE_PRONE, true);
        setBool(source, "enableSlide", MomentumServerConfig.ENABLE_SLIDE, true);
        setBool(source, "enableBreakFallReady", MomentumServerConfig.ENABLE_BREAK_FALL_READY, true);
        setBool(source, "enableBreakFall", MomentumServerConfig.ENABLE_BREAK_FALL, true);
        setBool(source, "enableDodge", MomentumServerConfig.ENABLE_DODGE, true);
        setBool(source, "enableWallClimb", MomentumServerConfig.ENABLE_WALL_CLIMB, true);
        setBool(source, "enableWallSlide", MomentumServerConfig.ENABLE_WALL_SLIDE, true);
        setBool(source, "enableWallRun", MomentumServerConfig.ENABLE_WALL_RUN, true);
        setBool(source, "enableWallHang", MomentumServerConfig.ENABLE_WALL_HANG, true);
        setBool(source, "enablePowerJump", MomentumServerConfig.ENABLE_POWER_JUMP, true);
        setBool(source, "enableWallKick", MomentumServerConfig.ENABLE_WALL_KICK, true);
        setBool(source, "enableVaultUp", MomentumServerConfig.ENABLE_VAULT_UP, true);
        setBool(source, "enableVaultIn", MomentumServerConfig.ENABLE_VAULT_IN, true);
        setBool(source, "enableWaterRun", MomentumServerConfig.ENABLE_WATER_RUN, true);
        setBool(source, "enableWaterPush", MomentumServerConfig.ENABLE_WATER_PUSH, true);
        setBool(source, "enableFallSlow", MomentumServerConfig.ENABLE_FALL_SLOW, true);
        setBool(source, "enableAirJump", MomentumServerConfig.ENABLE_AIR_JUMP, true);
    }

    private static JsonObject normalized() {
        JsonObject root = new JsonObject();
        JsonObject settings = new JsonObject();
        JsonObject switches = new JsonObject();
        root.add("functionSettings", settings);
        root.add("functionSwitches", switches);

        put(settings, "allowManeuverWithoutThruster", MomentumServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER);
        put(settings, "maneuverConsumeHunger", MomentumServerConfig.MANEUVER_CONSUME_HUNGER);
        put(settings, "defaultEnableManeuver", MomentumServerConfig.DEFAULT_ENABLE_MANEUVER);
        put(settings, "maneuverConsumeHungerAmount", MomentumServerConfig.MANEUVER_CONSUME_HUNGER_AMOUNT);
        put(settings, "boosterStaminaReduction", MomentumServerConfig.BOOSTER_STAMINA_REDUCTION);
        put(settings, "minSlideSpeed", MomentumServerConfig.MIN_SLIDE_SPEED);
        put(settings, "minWallRunSpeed", MomentumServerConfig.MIN_WALL_RUN_SPEED);
        put(settings, "minWaterSwimSpeed", MomentumServerConfig.MIN_WATER_SWIM_SPEED);
        put(settings, "slideAccelerationCooldown", MomentumServerConfig.SLIDE_ACCELERATION_COOLDOWN);
        put(settings, "airDodgeCooldown", MomentumServerConfig.DODGE_COOLDOWN);
        put(settings, "airDodgeStorage", MomentumServerConfig.DODGE_STORAGE);
        put(settings, "airFriction", MomentumServerConfig.AIR_FRICTION);
        put(settings, "airAccelerationXz", MomentumServerConfig.AIR_ACCELERATION_XZ);
        put(settings, "climbBoostMultiplier", MomentumServerConfig.CLIMB_BOOST_MULTIPLIER);
        put(settings, "wallKickAccelerationCooldown", MomentumServerConfig.WALL_KICK_ACCELERATION_COOLDOWN);

        putSwitches(switches);
        return root;
    }

    private static void putSwitches(JsonObject target) {
        put(target, "enableProne", MomentumServerConfig.ENABLE_PRONE);
        put(target, "enableSlide", MomentumServerConfig.ENABLE_SLIDE);
        put(target, "enableBreakFallReady", MomentumServerConfig.ENABLE_BREAK_FALL_READY);
        put(target, "enableBreakFall", MomentumServerConfig.ENABLE_BREAK_FALL);
        put(target, "enableDodge", MomentumServerConfig.ENABLE_DODGE);
        put(target, "enableWallClimb", MomentumServerConfig.ENABLE_WALL_CLIMB);
        put(target, "enableWallSlide", MomentumServerConfig.ENABLE_WALL_SLIDE);
        put(target, "enableWallRun", MomentumServerConfig.ENABLE_WALL_RUN);
        put(target, "enableWallHang", MomentumServerConfig.ENABLE_WALL_HANG);
        put(target, "enablePowerJump", MomentumServerConfig.ENABLE_POWER_JUMP);
        put(target, "enableWallKick", MomentumServerConfig.ENABLE_WALL_KICK);
        put(target, "enableVaultUp", MomentumServerConfig.ENABLE_VAULT_UP);
        put(target, "enableVaultIn", MomentumServerConfig.ENABLE_VAULT_IN);
        put(target, "enableWaterRun", MomentumServerConfig.ENABLE_WATER_RUN);
        put(target, "enableWaterPush", MomentumServerConfig.ENABLE_WATER_PUSH);
        put(target, "enableFallSlow", MomentumServerConfig.ENABLE_FALL_SLOW);
        put(target, "enableAirJump", MomentumServerConfig.ENABLE_AIR_JUMP);
    }

    private static void setBool(JsonObject source, String key, ConfigValue<Boolean> target, boolean fallback) {
        target.updateFromPlatform(FabricConfigIo.bool(source, key, fallback));
    }

    private static void setInt(JsonObject source, String key, ConfigValue<Integer> target, int fallback, int min, int max) {
        target.updateFromPlatform(FabricConfigIo.integer(source, key, fallback, min, max));
    }

    private static void setDouble(JsonObject source, String key, ConfigValue<Double> target, double fallback, double min, double max) {
        target.updateFromPlatform(FabricConfigIo.decimal(source, key, fallback, min, max));
    }

    private static void put(JsonObject target, String key, ConfigValue<?> value) {
        Object current = value.get();
        if (current instanceof Boolean bool) target.addProperty(key, bool);
        else if (current instanceof Number number) target.addProperty(key, number);
    }
}
