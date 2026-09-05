package com.akirahane.momentum;

import com.akirahane.momentum.client.init.InitSounds;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.MovementDamageHooks;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.platform.NeoForgeGameplayPlatform;
import com.akirahane.momentum.platform.PlatformServices;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

@Mod(value = Momentum.MODID)
@EventBusSubscriber(modid = Momentum.MODID)
public class Momentum {
    public static final String MODID = MomentumConstants.MOD_ID;
    protected static final Logger LOGGER = LogUtils.getLogger();

    public Momentum(IEventBus modEventBus, ModContainer modContainer) {
        PlatformServices.installGameplay(new NeoForgeGameplayPlatform());
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        InitItems.register(modEventBus);
        InitAttachments.register(modEventBus);
        InitSounds.SOUND_EVENTS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            ServerConfig.syncToCommon();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            ServerConfig.syncToCommon();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(
                    Component.translatable(player.getData(InitAttachments.MOMENTUM_ENABLED)
                            ? "message.momentum.momentum_enabled"
                            : "message.momentum.momentum_disabled"),
                    true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
            stateMachine.serverTick(player);
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        MovementDamageHooks.FallDamage adjusted = MovementDamageHooks.adjustFallDamage(
                player, event.getDistance(), event.getDamageMultiplier());
        event.setDistance(adjusted.distance());
        event.setDamageMultiplier(adjusted.multiplier());
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (MovementDamageHooks.shouldCancelDamage(player)) {
            event.setCanceled(true);
            player.fallDistance = 0;
        }
    }
}
