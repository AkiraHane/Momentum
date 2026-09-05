package com.akirahane.momentum.platform.config;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Loader-neutral view of a configuration value.
 *
 * <p>The loader adapter owns persistence and updates this value when its
 * configuration is loaded or reloaded. Gameplay code only reads it.</p>
 */
public final class ConfigValue<T> implements Supplier<T> {
    private volatile T value;

    public ConfigValue(T defaultValue) {
        this.value = Objects.requireNonNull(defaultValue);
    }

    @Override
    public T get() {
        return value;
    }

    public boolean getAsBoolean() {
        return (Boolean) value;
    }

    public int getAsInt() {
        return ((Number) value).intValue();
    }

    public void updateFromPlatform(T value) {
        this.value = Objects.requireNonNull(value);
    }
}
