package com.akirahane.momentum.core.common.state.states.ground;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.states.ground.action.ProneState;
import com.akirahane.momentum.core.common.state.states.ground.action.SlideState;
import com.akirahane.momentum.core.common.state.states.ground.action.WalkState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.Momentum.MODID;

public abstract class GroundState extends State {

    public static Identifier SLIDE_STEP_HEIGHT_ID = Identifier.fromNamespaceAndPath(MODID, "slide_step_height");

    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        // 输出速度
        if (context.isLowerCenter() &&
                player.getDeltaMovement().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get()
        ) {
            return SlideState.checkChildTransition(player, context);
        }
        if (context.isLowerCenter()) {
            return ProneState.checkChildTransition(player, context);
        }
        return WalkState.checkChildTransition(player, context);
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        // AttributeInstance有服务器到客户端的同步机制
        // 如果客户端也跑, 有概率会出现服务器同步过来后再次设置导致报错
        if (player instanceof ServerPlayer) {
            AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                attr.addTransientModifier(new AttributeModifier(
                        SLIDE_STEP_HEIGHT_ID,
                        0.5,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        if (player instanceof ServerPlayer) {
            AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                attr.removeModifier(SLIDE_STEP_HEIGHT_ID);
            }
        }
    }
}
