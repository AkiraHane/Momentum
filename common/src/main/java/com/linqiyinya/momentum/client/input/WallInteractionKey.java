package com.linqiyinya.momentum.client.input;

import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class WallInteractionKey {
    public static final String KEY_NAME = "key.momentum.wall_interaction";
    public static final Supplier<KeyMapping> WALL_INTERACTION = () -> KeyMapping.get(KEY_NAME);

    public static boolean isActive() {
        // 未绑定时自动允许墙面动作；绑定后，进入和维持动作均要求按住。
        KeyMapping key = WALL_INTERACTION.get();
        return key.isUnbound() || key.isDown();
    }
}
