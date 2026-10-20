package com.akirahane.momentum.client.input;

import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class LowerCenterKey {
    public static final String KEY_NAME = "key.momentum.lower_center";
    public static final Supplier<KeyMapping> LOWER_CENTER = () -> KeyMapping.get(KEY_NAME);
}
