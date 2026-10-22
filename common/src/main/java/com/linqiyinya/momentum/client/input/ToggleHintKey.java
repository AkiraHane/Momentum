package com.linqiyinya.momentum.client.input;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.client.hud.HintManager;
import net.minecraft.client.KeyMapping;

import java.util.function.Supplier;

public class ToggleHintKey {
    public static final String KEY_NAME = "key.momentum.toggle_hint";
    public static final Supplier<KeyMapping> TOGGLE_HINT_KEY_MAPPING = () -> KeyMapping.get(KEY_NAME);

    private static boolean wasDown = false;

    public static void onClientTick(boolean isDown) {
        if (isDown && !wasDown) {
            HintManager.toggleVisible();
            if (HintManager.isVisible()) {
                HintManager.forceShow();
            }
            Constants.LOG.debug("[INPUT] TOGGLE_HINTS side=client visible={}", HintManager.isVisible());
        }
        wasDown = isDown;
    }

}
