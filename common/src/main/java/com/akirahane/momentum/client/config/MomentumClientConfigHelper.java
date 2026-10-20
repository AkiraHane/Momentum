package com.akirahane.momentum.client.config;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.client.hud.HintManager;

public class MomentumClientConfigHelper {

    public static void onConfigLoad() {
        HintManager.setVisible(ClientConfig.ENABLE_KEY_HINTS.getAsBoolean());
        Constants.LOG.debug("[CONFIG] CLIENT_LOAD keyHints={} prone={} slide={} cameraOffset={}",
                ClientConfig.ENABLE_KEY_HINTS.getAsBoolean(), ClientConfig.ENABLE_PRONE.getAsBoolean(),
                ClientConfig.ENABLE_SLIDE.getAsBoolean(), ClientConfig.ENABLE_CAMERA_OFFSET.getAsBoolean());
    }
}
