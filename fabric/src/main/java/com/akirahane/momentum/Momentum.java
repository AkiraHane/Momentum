package com.akirahane.momentum;

import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.utils.MomentumEventHelper;
import com.akirahane.momentum.network.MomentumNetwork;
import com.akirahane.momentum.registry.MomentumAttachments;
import com.akirahane.momentum.registry.MomentumItemRegistration;
import com.akirahane.momentum.registry.MomentumSoundRegistration;
import com.akirahane.momentum.platform.Services;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;

public class Momentum implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        MomentumItemRegistration.register();
        MomentumSoundRegistration.register();
        MomentumAttachments.register();
        MomentumNetwork.register();
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> onPlayerLogin(listener.player));
        ServerTickEvents.END_SERVER_TICK.register(Momentum::onServerTick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Momentum::onPlayerHurt);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        MomentumEventHelper.onPlayerLogin(player, Services.PLATFORM.isMomentumEnabled(player));
    }

    public static void onServerTick(MinecraftServer server) {
        MomentumEventHelper.onServerTick(server, ServerPlayNetworking::send);
    }

    public static boolean onPlayerHurt(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Player player)) return true;

        return !MomentumEventHelper.onPlayerHurt(player);
    }
}
