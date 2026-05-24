package com.akirahane.momentum;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.init.InitAttachments;
import com.akirahane.momentum.core.init.InitItems;
import com.akirahane.momentum.server.config.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(value = Momentum.MODID)
public class Momentum {
    public static final String MODID = "momentum";

    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        InitItems.register(modEventBus);
        InitAttachments.register(modEventBus);
    }
}
