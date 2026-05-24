package com.akirahane.momentum.client;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.init.InitAttachments;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// 此类不会在专用服务器上加载。在此处访问客户端代码是安全的。
@Mod(value = Momentum.MODID, dist = Dist.CLIENT)
// 你可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class MomentumClient {

    public MomentumClient(ModContainer modContainer) {
        // 注册我们 mod 的 ModConfigSpec，以便 FML 可以为我们创建和加载配置文件
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // 客户端驱动（用于预测和视觉效果）
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer player && player.getData(InitAttachments.MOMENTUM_ENABLED)) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            sm.clientTick(player);
        }
    }
}