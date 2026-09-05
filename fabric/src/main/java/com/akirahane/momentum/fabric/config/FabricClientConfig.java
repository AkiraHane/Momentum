package com.akirahane.momentum.fabric.config;

import com.akirahane.momentum.platform.config.ConfigValue;
import com.akirahane.momentum.platform.config.MomentumClientConfig;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricClientConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("momentum-client.json");

    private FabricClientConfig() {
    }

    public static void load() {
        JsonObject root = FabricConfigIo.read(PATH);
        if (root == null) {
            return;
        }

        JsonObject general = FabricConfigIo.child(root, "general");
        JsonObject hints = FabricConfigIo.child(root, "keyHints");
        JsonObject switches = FabricConfigIo.child(root, "functionSwitches");

        setBool(general, "enableCameraOffset", MomentumClientConfig.ENABLE_CAMERA_OFFSET, true);
        setDouble(general, "fovBonusMax", MomentumClientConfig.FOV_BONUS_MAX, 0.0, 0.0, 30.0);
        setBool(general, "enableDodgeDirDouble", MomentumClientConfig.ENABLE_DODGE_DIR_DOUBLE, true);
        setBool(general, "enableDodgeSprintClick", MomentumClientConfig.ENABLE_DODGE_SPRINT_CLICK, false);
        setBool(general, "enableDodgeSprintDouble", MomentumClientConfig.ENABLE_DODGE_SPRINT_DOUBLE, false);

        setBool(hints, "enableKeyHints", MomentumClientConfig.ENABLE_KEY_HINTS, true);
        setDouble(hints, "minAlphaWhenMoving", MomentumClientConfig.MIN_ALPHA_WHEN_MOVING, 0.4, 0.0, 1.0);
        setDouble(hints, "maxAlpha", MomentumClientConfig.MAX_ALPHA, 1.0, 0.0, 1.0);
        setInt(hints, "freshDuration", MomentumClientConfig.FRESH_DURATION, 80, 0, Integer.MAX_VALUE);
        setInt(hints, "idleDelay", MomentumClientConfig.IDLE_DELAY, 100, 0, Integer.MAX_VALUE);
        setDouble(hints, "moveThresholdSqr", MomentumClientConfig.MOVE_THRESHOLD_SQR, 0.005, 0.0, Double.MAX_VALUE);
        setDouble(hints, "fadeInSpeed", MomentumClientConfig.FADE_IN_SPEED, 0.01, 0.0, 1.0);
        setDouble(hints, "fadeOutSpeed", MomentumClientConfig.FADE_OUT_SPEED, 0.18, 0.0, 1.0);

        setSwitches(switches);
        FabricConfigIo.write(PATH, normalized());
    }

    public static void save() {
        FabricConfigIo.write(PATH, normalized());
    }

    private static void setSwitches(JsonObject source) {
        setBool(source, "enableProne", MomentumClientConfig.ENABLE_PRONE, true);
        setBool(source, "enableSlide", MomentumClientConfig.ENABLE_SLIDE, true);
        setBool(source, "enableBreakFallReady", MomentumClientConfig.ENABLE_BREAK_FALL_READY, true);
        setBool(source, "enableBreakFall", MomentumClientConfig.ENABLE_BREAK_FALL, true);
        setBool(source, "enableDodge", MomentumClientConfig.ENABLE_DODGE, true);
        setBool(source, "enableWallClimb", MomentumClientConfig.ENABLE_WALL_CLIMB, true);
        setBool(source, "enableWallSlide", MomentumClientConfig.ENABLE_WALL_SLIDE, true);
        setBool(source, "enableWallRun", MomentumClientConfig.ENABLE_WALL_RUN, true);
        setBool(source, "enableWallHang", MomentumClientConfig.ENABLE_WALL_HANG, true);
        setBool(source, "enablePowerJump", MomentumClientConfig.ENABLE_POWER_JUMP, true);
        setBool(source, "enableWallKick", MomentumClientConfig.ENABLE_WALL_KICK, true);
        setBool(source, "enableVaultUp", MomentumClientConfig.ENABLE_VAULT_UP, true);
        setBool(source, "enableVaultIn", MomentumClientConfig.ENABLE_VAULT_IN, true);
        setBool(source, "enableWaterRun", MomentumClientConfig.ENABLE_WATER_RUN, true);
        setBool(source, "enableWaterPush", MomentumClientConfig.ENABLE_WATER_PUSH, true);
        setBool(source, "enableFallSlow", MomentumClientConfig.ENABLE_FALL_SLOW, true);
        setBool(source, "enableAirJump", MomentumClientConfig.ENABLE_AIR_JUMP, true);
    }

    private static JsonObject normalized() {
        JsonObject root = new JsonObject();
        JsonObject general = new JsonObject();
        JsonObject hints = new JsonObject();
        JsonObject switches = new JsonObject();
        root.add("general", general);
        root.add("keyHints", hints);
        root.add("functionSwitches", switches);

        put(general, "enableCameraOffset", MomentumClientConfig.ENABLE_CAMERA_OFFSET);
        put(general, "fovBonusMax", MomentumClientConfig.FOV_BONUS_MAX);
        put(general, "enableDodgeDirDouble", MomentumClientConfig.ENABLE_DODGE_DIR_DOUBLE);
        put(general, "enableDodgeSprintClick", MomentumClientConfig.ENABLE_DODGE_SPRINT_CLICK);
        put(general, "enableDodgeSprintDouble", MomentumClientConfig.ENABLE_DODGE_SPRINT_DOUBLE);

        put(hints, "enableKeyHints", MomentumClientConfig.ENABLE_KEY_HINTS);
        put(hints, "minAlphaWhenMoving", MomentumClientConfig.MIN_ALPHA_WHEN_MOVING);
        put(hints, "maxAlpha", MomentumClientConfig.MAX_ALPHA);
        put(hints, "freshDuration", MomentumClientConfig.FRESH_DURATION);
        put(hints, "idleDelay", MomentumClientConfig.IDLE_DELAY);
        put(hints, "moveThresholdSqr", MomentumClientConfig.MOVE_THRESHOLD_SQR);
        put(hints, "fadeInSpeed", MomentumClientConfig.FADE_IN_SPEED);
        put(hints, "fadeOutSpeed", MomentumClientConfig.FADE_OUT_SPEED);

        putSwitches(switches);
        return root;
    }

    private static void putSwitches(JsonObject target) {
        put(target, "enableProne", MomentumClientConfig.ENABLE_PRONE);
        put(target, "enableSlide", MomentumClientConfig.ENABLE_SLIDE);
        put(target, "enableBreakFallReady", MomentumClientConfig.ENABLE_BREAK_FALL_READY);
        put(target, "enableBreakFall", MomentumClientConfig.ENABLE_BREAK_FALL);
        put(target, "enableDodge", MomentumClientConfig.ENABLE_DODGE);
        put(target, "enableWallClimb", MomentumClientConfig.ENABLE_WALL_CLIMB);
        put(target, "enableWallSlide", MomentumClientConfig.ENABLE_WALL_SLIDE);
        put(target, "enableWallRun", MomentumClientConfig.ENABLE_WALL_RUN);
        put(target, "enableWallHang", MomentumClientConfig.ENABLE_WALL_HANG);
        put(target, "enablePowerJump", MomentumClientConfig.ENABLE_POWER_JUMP);
        put(target, "enableWallKick", MomentumClientConfig.ENABLE_WALL_KICK);
        put(target, "enableVaultUp", MomentumClientConfig.ENABLE_VAULT_UP);
        put(target, "enableVaultIn", MomentumClientConfig.ENABLE_VAULT_IN);
        put(target, "enableWaterRun", MomentumClientConfig.ENABLE_WATER_RUN);
        put(target, "enableWaterPush", MomentumClientConfig.ENABLE_WATER_PUSH);
        put(target, "enableFallSlow", MomentumClientConfig.ENABLE_FALL_SLOW);
        put(target, "enableAirJump", MomentumClientConfig.ENABLE_AIR_JUMP);
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
