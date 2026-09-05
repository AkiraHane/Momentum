package com.akirahane.momentum.fabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class FabricConfigIo {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private FabricConfigIo() {
    }

    static JsonObject read(Path path) {
        if (!Files.exists(path)) {
            return new JsonObject();
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new IOException("Root value must be a JSON object");
            }
            return root.getAsJsonObject();
        } catch (Exception exception) {
            LOGGER.error("Could not read Momentum config {}; using defaults without overwriting it", path, exception);
            return null;
        }
    }

    static JsonObject child(JsonObject root, String name) {
        JsonElement value = root.get(name);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    static boolean bool(JsonObject object, String name, boolean fallback) {
        try {
            JsonElement value = object.get(name);
            return value == null ? fallback : value.getAsBoolean();
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    static int integer(JsonObject object, String name, int fallback, int min, int max) {
        try {
            JsonElement value = object.get(name);
            return value == null ? fallback : Math.clamp(value.getAsInt(), min, max);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    static double decimal(JsonObject object, String name, double fallback, double min, double max) {
        try {
            JsonElement value = object.get(name);
            double parsed = value == null ? fallback : value.getAsDouble();
            return Double.isFinite(parsed) ? Math.clamp(parsed, min, max) : fallback;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    static String toJson(JsonObject root) {
        return GSON.toJson(root);
    }

    static JsonObject parse(String json) {
        try {
            JsonElement root = JsonParser.parseString(json);
            return root.isJsonObject() ? root.getAsJsonObject() : null;
        } catch (RuntimeException exception) {
            LOGGER.error("Could not parse synchronized Momentum server config", exception);
            return null;
        }
    }

    static void write(Path path, JsonObject root) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Could not write Momentum config {}", path, exception);
        }
    }
}
