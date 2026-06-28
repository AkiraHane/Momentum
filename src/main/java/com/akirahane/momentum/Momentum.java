package com.akirahane.momentum;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.client.init.InitSounds;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
        InitSounds.SOUND_EVENTS.register(modEventBus);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.sendSystemMessage(
                    Component.translatable(player.getData(InitAttachments.MOMENTUM_ENABLED)
                            ? "message.momentum.momentum_enabled"
                            : "message.momentum.momentum_disabled"),
                    true
            );
        }
    }

    // 服务端驱动 状态机处理放在原版逻辑之前, 但是要晚于玩家输入处理
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            sm.serverTick(player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
            if (stateMachine.isDirty()) {
                StateBroadcastPacket packet = new StateBroadcastPacket(player.getId(), stateMachine.getCurrentState().getStateType(), stateMachine.getContext().getTransitionExtraData(), stateMachine.getContext().getTransitionWallData());
                for (ServerPlayer observer : player.level().players()) {
                    // 不排除自己: Replay Mod 录制需要录制者也收到此包, 才能在回放中重放状态转换
                    // 客户端 handler 中有 player == mc.player 的防护, 正常游戏不会重复处理
                    PacketDistributor.sendToPlayer(observer, packet);
                }
                stateMachine.setDirty(false);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        float distance = (float) event.getDistance();
        float multiplier = event.getDamageMultiplier();

        if (stateMachine.getContext().isHasJetBooster()){
            distance -= 12;
            multiplier *= 0.5F;
        }
        if (stateMachine.getContext().getBreakFallReadyCount() > 0) {
            distance -= 6;
            multiplier *= 0.3F;
            if (event.getDistance() > Math.min(4, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))){
                stateMachine.getContext().setToBreakFallState(true);
            }
        } else if (stateMachine.getContext().getBreakFallReadyCount() == 0) {
            distance -= 3;
            multiplier *= 0.6F;
            if (event.getDistance() > Math.min(4, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))){
                stateMachine.getContext().setToBreakFallState(true);
            }
        }

        event.setDistance(Math.max(0, distance));
        event.setDamageMultiplier(multiplier);
    }

    // 监听伤害
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (StateType.DODGE.equals(stateMachine.getCurrentState().getStateType())) {
            event.setCanceled(true);
            player.fallDistance = 0;
        }
    }
}
