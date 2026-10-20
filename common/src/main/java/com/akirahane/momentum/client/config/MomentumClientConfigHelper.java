package com.akirahane.momentum.client.config;

import com.akirahane.momentum.client.hud.HintManager;

public class MomentumClientConfigHelper {

    public static void onConfigLoad() {
        HintManager.setVisible(ClientConfig.ENABLE_KEY_HINTS.getAsBoolean());
    }
}
