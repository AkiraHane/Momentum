package com.akirahane.momentum.client.config;

import com.akirahane.momentum.Constants;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class MomentumClientConfig implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModConfigEvents.loading(Constants.MOD_ID).register(config -> {
            if (config.getSpec() == ClientConfig.SPEC) {
                MomentumClientConfigHelper.onConfigLoad();
            }
        });
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ConfigScreenFactoryRegistry.INSTANCE.register(Constants.MOD_ID, ConfigurationScreen::new);
    }
}
