package com.akirahane.momentum.client;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.debug.MovementDebugEntry;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

// 此类不会在专用服务器上加载。在此处访问客户端代码是安全的。
@Mod(value = Momentum.MODID, dist = Dist.CLIENT)
// 可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class MomentumClient {
    protected static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier MOVEMENT_INFO =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "movement_info");
    public static final Identifier MOVEMENT_ANIM =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "movement_anim");

    public MomentumClient(ModContainer modContainer) {
        // 注册 mod 的 ModConfigSpec，以便 FML 可以创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    // 客户端驱动（用于移动和视觉效果）
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            BaseState state = sm.clientTick(player);
            if (state != null) {
                ClientPacketDistributor.sendToServer(new StateTransitionPacket(state.getStateType()));
            }
        }
    }

    // 记录这一tick的真实位移作为单位速度，同时记录游戏速度，用于下一tick计算
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            sm.getContext().setOldSpeed(sm.getContext().getSpeed());
            sm.getContext().setSpeed(new Vec3(
                    player.getX() - player.xOld,
                    player.getY() - player.yOld,
                    player.getZ() - player.zOld
            ));
            sm.getContext().setOldDeltaMovement(player.getDeltaMovement());
        }
    }

    @SubscribeEvent
    public static void onRegisterDebugEntries(RegisterDebugEntriesEvent event) {
        event.register(MOVEMENT_INFO, new MovementDebugEntry());
        event.includeInProfile(MOVEMENT_INFO, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.NEVER);
    }

    // https://docs.zigythebird.com/pal/how_to_play_animations
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    MOVEMENT_ANIM, 1500, // 1500 优先级，gameplay 动画
                    player -> new PlayerAnimationController(player,
                            (controller, state, animSetter) -> PlayState.STOP
                    )
            );
        });
    }
}