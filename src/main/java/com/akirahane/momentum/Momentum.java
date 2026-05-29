package com.akirahane.momentum;

import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(value = Momentum.MODID)
@EventBusSubscriber(modid = Momentum.MODID)
public class Momentum {
    public static final String MODID = "momentum";
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        InitItems.register(modEventBus);
        InitAttachments.register(modEventBus);
    }
}
