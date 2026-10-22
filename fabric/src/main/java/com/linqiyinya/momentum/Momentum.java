package com.linqiyinya.momentum;

import com.linqiyinya.momentum.config.ServerConfig;
import com.linqiyinya.momentum.config.MomentumConfigHelper;
import com.linqiyinya.momentum.core.utils.MomentumEventHelper;
import com.linqiyinya.momentum.network.MomentumNetwork;
import com.linqiyinya.momentum.network.MomentumNetworkHelper;
import com.linqiyinya.momentum.network.StateBroadcastPacket;
import com.linqiyinya.momentum.registry.MomentumAttachments;
import com.linqiyinya.momentum.registry.MomentumItemRegistration;
import com.linqiyinya.momentum.registry.MomentumSoundRegistration;
import com.linqiyinya.momentum.platform.Services;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.config.ModConfig;

public class Momentum implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConfigEvents.loading(Constants.MOD_ID).register(config -> {
            if (config.getSpec() == ServerConfig.SPEC) {
                MomentumConfigHelper.onServerConfigLoad();
            }
        });
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        MomentumItemRegistration.register();
        MomentumSoundRegistration.register();
        MomentumAttachments.register();
        MomentumNetwork.register();
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> onPlayerLogin(listener.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> MomentumEventHelper.onPlayerRespawn(newPlayer));
        EntityTrackingEvents.START_TRACKING.register(Momentum::onStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(Momentum::onStopTracking);
        ServerTickEvents.END_SERVER_TICK.register(Momentum::onServerTick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(Momentum::onPlayerHurt);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        MomentumEventHelper.onPlayerLogin(player, Services.PLATFORM.isMomentumEnabled(player));
    }

    public static void onServerTick(MinecraftServer server) {
        MomentumEventHelper.onServerTick(server, (player, packet) -> {
            for (ServerPlayer observer : PlayerLookup.tracking(player)) {
                if (observer != player) {
                    ServerPlayNetworking.send(observer, packet);
                }
            }
            // 自身副本供 Replay Mod 录制状态转换。
            ServerPlayNetworking.send(player, packet);
        });
    }

    public static void onStartTracking(Entity entity, ServerPlayer observer) {
        if (entity instanceof Player player) {
            StateBroadcastPacket packet = MomentumNetworkHelper.stateSnapshot(player);
            ServerPlayNetworking.send(observer, packet);
            Constants.LOG.debug("[NET] TRACK_SNAPSHOT side=server player={} observer={} dimension={} state={} extra={} wall={}",
                    player.getId(), observer.getId(), player.level().dimension().identifier(), packet.stateType(), packet.extraData(), packet.wallData());
        }
    }

    public static void onStopTracking(Entity entity, ServerPlayer observer) {
        if (entity instanceof Player player) {
            Constants.LOG.debug("[NET] TRACK_STOP side=server player={} observer={} dimension={}",
                    player.getId(), observer.getId(), player.level().dimension().identifier());
        }
    }

    public static boolean onPlayerHurt(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Player player)) return true;

        return !MomentumEventHelper.onPlayerHurt(player);
    }
}
