package com.akirahane.momentum.client;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.debug.MovementDebugEntry;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import static com.akirahane.momentum.client.config.ClientConfig.ENABLE_CAMERA_OFFSET;
import static com.akirahane.momentum.client.config.ClientConfig.ENABLE_KEY_HINTS;

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

    public MomentumClient(ModContainer modContainer, IEventBus modEventBus) {
        // 注册 mod 的 ModConfigSpec，以便 FML 可以创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        HintManager.setVisible(ENABLE_KEY_HINTS.getAsBoolean());
    }

    // 客户端驱动（用于移动和视觉效果）
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            // 本地玩家
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            BaseState state = sm.clientTick(player);
            if (state != null) {
                ClientPacketDistributor.sendToServer(
                    new StateTransitionPacket(state.getStateType(), sm.getContext().getTransitionExtraData())
                );
                sm.getContext().setTransitionExtraData(-1); // 重置
            }
        } else if (event.getEntity() instanceof AbstractClientPlayer otherPlayer) {
            // 其他联机玩家
            MovementStateMachine sm = otherPlayer.getData(InitAttachments.MOVEMENT_STATE);
            sm.clientTickRemote(otherPlayer);
        }
    }

    // 记录这一tick的真实位移作为单位速度，同时记录游戏速度，用于下一tick计算
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
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
    // 继承一个拿moLang方便调试变量
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    MOVEMENT_ANIM, 1500, // 1500 优先级，gameplay 动画
                    player -> new MomentumAnimationController(player,
                            (controller, state, animSetter) -> PlayState.STOP
                    )
            );
        });
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;
        HintManager.clientTick(mc.player);
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ENABLE_CAMERA_OFFSET.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasData(InitAttachments.MOVEMENT_STATE)) return;

        var context = mc.player.getData(InitAttachments.MOVEMENT_STATE).getContext();

        float partialTick = (float) event.getPartialTick();
        float roll = context.getRenderCameraRoll(partialTick);
        // 动量倾斜（滑铲、闪避等）
        float momentumRoll = context.getRenderMomentumRoll(partialTick);

        if (roll + momentumRoll != 0F) {
            event.setRoll(event.getRoll() + roll + momentumRoll);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (!ENABLE_CAMERA_OFFSET.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasData(InitAttachments.MOVEMENT_STATE)) return;

        var context = mc.player.getData(InitAttachments.MOVEMENT_STATE).getContext();
        float bonus = context.getRenderFovBonus((float) event.getPartialTick());

        if (bonus != 0F) {
            event.setFOV(event.getFOV() + bonus);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.hasData(InitAttachments.MOVEMENT_STATE)) return;
        if (!mc.player.hasData(InitAttachments.MOVEMENT_STATE)) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!player.hasData(InitAttachments.MOVEMENT_STATE)) return;

        var machine = mc.player.getData(InitAttachments.MOVEMENT_STATE);
        var context = machine.getContext();
        float pt = event.getPartialTick();

        PoseStack pose = event.getPoseStack();
        pose.translate(0, context.getRenderArmOffsetY(pt), 0);
        pose.mulPose(Axis.XP.rotationDegrees(context.getRenderArmRotX(pt)));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(InitItems.JET_BOOSTER_ITEM)) {
            event.getToolTip().add(Component.translatable("item.momentum.jet_booster.tooltip"));
        }
    }
}