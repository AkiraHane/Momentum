package com.akirahane.momentum.client;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.config.MomentumClientConfigHelper;
import com.akirahane.momentum.client.debug.MomentumDebugEntry;
import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.registry.MomentumItems;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
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
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import static com.akirahane.momentum.client.config.ClientConfig.ENABLE_CAMERA_OFFSET;

// 此类不会在专用服务器上加载。在此处访问客户端代码是安全的。
@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
// 可以使用 EventBusSubscriber 来自动注册类中所有带有 @SubscribeEvent 注解的静态方法
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class MomentumClient {
    protected static final Logger LOGGER = LogUtils.getLogger();

    public static final Identifier MOMENTUM_INFO =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "movement_info");

    public MomentumClient(ModContainer modContainer, IEventBus modEventBus) {
        // 注册 mod 的 ModConfigSpec，以便 FML 可以创建和加载配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ClientConfig.SPEC) {
            MomentumClientConfigHelper.onConfigLoad();
        }
    }

    // 客户端驱动（用于移动和视觉效果）
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof LocalPlayer player) {
            // 本地玩家
            MomentumStateMachine sm = Services.PLATFORM.getOrCreateStateMachine(player);
            BaseState state = sm.clientTick(player);
            if (state != null) {
                Services.PLATFORM.sendToServer(
                    new StateTransitionPacket(state.getStateType(), sm.getContext().getTransitionExtraData(), sm.getContext().getTransitionWallData())
                );
                sm.getContext().setTransitionExtraData(-1); // 重置
                sm.getContext().setTransitionWallData((byte)-1); // 重置
            }
        } else if (event.getEntity() instanceof AbstractClientPlayer otherPlayer) {
            // 其他联机玩家
            MomentumStateMachine sm = Services.PLATFORM.getOrCreateStateMachine(otherPlayer);
            sm.clientTickRemote(otherPlayer);
        }
    }

    // 记录这一tick的真实位移作为单位速度，同时记录游戏速度，用于下一tick计算
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            MomentumStateMachine sm = Services.PLATFORM.getOrCreateStateMachine(player);
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
        event.register(MOMENTUM_INFO, new MomentumDebugEntry());
        event.includeInProfile(MOMENTUM_INFO, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.NEVER);
    }

    // 渲染层统一保险：原版模式不施加任何相机/渲染修改
    private static boolean isOriginal(Minecraft mc) {
        if (mc.player == null) return true;
        if (!Services.PLATFORM.hasStateMachine(mc.player)) return true;
        return Services.PLATFORM.getOrCreateStateMachine(mc.player).getCurrentState().getStateType() == StateType.ORIGINAL;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ENABLE_CAMERA_OFFSET.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (isOriginal(mc)) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var context = Services.PLATFORM.getOrCreateStateMachine(player).getContext();

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
        if (isOriginal(mc)) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var context = Services.PLATFORM.getOrCreateStateMachine(player).getContext();
        float bonus = context.getRenderFovBonus((float) event.getPartialTick());

        if (bonus != 0F) {
            event.setFOV(event.getFOV() + bonus);
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (isOriginal(mc)) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var machine = Services.PLATFORM.getOrCreateStateMachine(player);
        var context = machine.getContext();
        float pt = event.getPartialTick();

        PoseStack pose = event.getPoseStack();
        pose.translate(0, context.getRenderArmOffsetY(pt), 0);
        pose.mulPose(Axis.XP.rotationDegrees(context.getRenderArmRotX(pt)));
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(MomentumItems.JET_BOOSTER_ITEM.get())) {
            event.getToolTip().add(Component.translatable("item.momentum.jet_booster.tooltip"));
        }
    }
}
