package com.akirahane.momentum;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.init.ModAttachments;
import com.akirahane.momentum.core.init.ModItems;
import com.akirahane.momentum.server.config.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(value = Momentum.MODID)
public class Momentum {
    public static final String MODID = "momentum";

    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        // 注册我们 mod 的 ModConfigSpec，以便 FML 可以为我们创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
