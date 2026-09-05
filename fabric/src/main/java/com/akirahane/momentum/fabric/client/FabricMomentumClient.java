package com.akirahane.momentum.fabric.client;

import com.akirahane.momentum.MomentumConstants;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.client.debug.MovementDebugEntry;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.fabric.client.platform.FabricClientPlatform;
import com.akirahane.momentum.fabric.client.hud.FabricHintHud;
import com.akirahane.momentum.fabric.client.hud.FabricHintManager;
import com.akirahane.momentum.fabric.config.FabricClientConfig;
import com.akirahane.momentum.fabric.init.FabricItems;
import com.akirahane.momentum.fabric.network.FabricClientNetwork;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.akirahane.momentum.platform.PlatformServices;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class FabricMomentumClient implements ClientModInitializer {
    private static final Identifier MOVEMENT_ANIM = Identifier.fromNamespaceAndPath(
            MomentumConstants.MOD_ID, "movement_anim");
    private static boolean toggleMomentumDown;
    private static boolean toggleHintsDown;

    @Override
    public void onInitializeClient() {
        FabricClientConfig.load();
        FabricKeyMappings.register();
        FabricHintManager.initialize();
        FabricHintHud.register();
        PlatformServices.installClient(new FabricClientPlatform(FabricKeyMappings.LOWER_CENTER));
        FabricClientNetwork.register();
        registerClientTicks();
        registerAnimationFactory();
        registerItemTooltip();
        registerDebugEntry();
    }

    private static void registerAnimationFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                MOVEMENT_ANIM,
                1500,
                avatar -> avatar instanceof Player player
                        ? new MomentumAnimationController(
                                player, (controller, state, animSetter) -> PlayState.STOP)
                        : null
        );
    }

    private static void registerItemTooltip() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
            if (stack.is(FabricItems.JET_BOOSTER)) {
                lines.add(Component.translatable("item.momentum.jet_booster.tooltip"));
            }
        });
    }

    private static void registerDebugEntry() {
        DebugScreenEntries.register(MovementDebugEntry.ID, new MovementDebugEntry());
    }

    private static void registerClientTicks() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                toggleMomentumDown = false;
                toggleHintsDown = false;
                return;
            }

            boolean toggleDown = FabricKeyMappings.TOGGLE_MOMENTUM.isDown()
                    && client.options.keyShift.isDown();
            if (toggleDown && !toggleMomentumDown) {
                FabricClientNetwork.sendToggle();
            }
            toggleMomentumDown = toggleDown;

            boolean hintsDown = FabricKeyMappings.TOGGLE_HINTS.isDown()
                    && client.options.keyShift.isDown();
            if (hintsDown && !toggleHintsDown) {
                FabricHintManager.toggleVisible();
            }
            toggleHintsDown = hintsDown;

            for (Player player : client.level.players()) {
                MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(player);
                if (player instanceof LocalPlayer) {
                    BaseState transitioned = stateMachine.clientTick(player);
                    if (transitioned != null) {
                        FabricClientNetwork.sendStateTransition(new StateTransitionPacket(
                                transitioned.getStateType(),
                                stateMachine.getContext().getTransitionExtraData(),
                                stateMachine.getContext().getTransitionWallData()));
                        stateMachine.getContext().setTransitionExtraData(-1);
                        stateMachine.getContext().setTransitionWallData((byte) -1);
                    }
                } else {
                    stateMachine.clientTickRemote(player);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) {
                return;
            }
            for (Player player : client.level.players()) {
                var context = PlatformServices.gameplay().movementState(player).getContext();
                context.setOldSpeed(context.getSpeed());
                context.setSpeed(new Vec3(
                        player.getX() - player.xOld,
                        player.getY() - player.yOld,
                        player.getZ() - player.zOld));
                context.setOldDeltaMovement(player.getDeltaMovement());
            }
            if (client.player != null && !client.isPaused()) {
                FabricHintManager.clientTick(client.player);
            }
        });
    }
}
