package com.linqiyinya.momentum.client.animation;

import net.fabricmc.api.ClientModInitializer;

public class MomentumClientAnimation implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MomentumAnimationRegistration.register();
    }
}
