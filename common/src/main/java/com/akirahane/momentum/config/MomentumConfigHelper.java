package com.akirahane.momentum.config;

import com.akirahane.momentum.Constants;

public class MomentumConfigHelper {

    public static void onServerConfigLoad() {
        Constants.LOG.debug("[CONFIG] SERVER_LOAD prone={} slide={} allowWithoutBooster={} defaultEnabled={}",
                ServerConfig.ENABLE_PRONE.getAsBoolean(), ServerConfig.ENABLE_SLIDE.getAsBoolean(),
                ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.getAsBoolean(), ServerConfig.DEFAULT_ENABLE_MANEUVER.getAsBoolean());
    }
}
