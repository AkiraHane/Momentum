package com.akirahane.momentum;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
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
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        InitItems.register(modEventBus);
        InitAttachments.register(modEventBus);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // 状态机处理放在原版逻辑之前, 但是要晚于玩家输入处理
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                sm.serverTick(serverPlayer);
            }
            if (event.getEntity() instanceof LocalPlayer localPlayer) {
                sm.clientTick(localPlayer);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            Vec3 actualMovement = new Vec3(
                    player.getX() - player.xo,
                    player.getY() - player.yo,
                    player.getZ() - player.zo
            );
            sm.getContext().setSpeed(actualMovement);
        }
    }
}
