package com.akirahane.momentum;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// 此类不会在专用服务器上加载。在此处访问客户端代码是安全的。
@Mod(value = Momentum.MODID, dist = Dist.CLIENT)
// 你可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class MomentumClient {

    public MomentumClient(ModContainer container) {
        // 允许 NeoForge 为此模组的配置创建配置界面。
        // 要访问配置界面，请前往“模组”界面 > 点击你的模组 > 点击“配置”。
        // 请勿忘记将你的配置选项的翻译添加到 en_us.json 文件中。
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}