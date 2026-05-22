package com.akirahane.momentum;

import com.akirahane.momentum.config.ClientConfig;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.init.ModAttachments;
import com.akirahane.momentum.init.ModItems;
import net.neoforged.fml.config.ModConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(Momentum.MODID)
public class Momentum {
    public static final String MODID = "momentum";

    // mod 类的构造函数是 mod 加载时运行的第一段代码。
    // FML 会识别某些参数类型（如 IEventBus 或 ModContainer）并自动传入。
    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        // 注册我们 mod 的 ModConfigSpec，以便 FML 可以为我们创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
    }
}
