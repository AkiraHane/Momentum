package com.akirahane.momentum;

import com.akirahane.momentum.core.utils.MomentumEventHelper;
import com.akirahane.momentum.registry.MomentumAttachments;
import com.akirahane.momentum.registry.MomentumItems;
import com.akirahane.momentum.registry.MomentumSounds;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static com.akirahane.momentum.Constants.MOD_ID;

@Mod(value = MOD_ID)
@EventBusSubscriber(modid = MOD_ID)
public class Momentum {

    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        MomentumItems.register(modEventBus);
        MomentumAttachments.register(modEventBus);
        MomentumSounds.SOUND_EVENTS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MomentumEventHelper.onPlayerLogin(player, Services.PLATFORM.isMomentumEnabled(player));
        }
    }

    // 服务端驱动 状态机处理放在原版逻辑之前, 但是要晚于玩家输入处理
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MomentumEventHelper.onPlayerTick(player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MomentumEventHelper.onServerTick(
                event.getServer(),
                PacketDistributor::sendToPlayer
        );
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MomentumEventHelper.FallResult result = MomentumEventHelper.onLivingFall(
                player, event.getDistance(), event.getDamageMultiplier()
        );
        event.setDistance(result.distance());
        event.setDamageMultiplier(result.damageMultiplier());
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (MomentumEventHelper.onPlayerHurt(player)) {
            event.setCanceled(true);
        }
    }
}
