package com.akirahane.momentum.core.common.state.states;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.states.movements.AirborneState;
import com.akirahane.momentum.core.common.state.states.movements.GroundState;
import com.akirahane.momentum.core.common.context.PlayerMovementContext;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.Momentum.MODID;

public abstract class MovementState extends State {

    public static Identifier SLIDE_STEP_HEIGHT_ID = Identifier.fromNamespaceAndPath(MODID, "slide_step_height");
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (player.onGround()) {
            return GroundState.checkChildTransition(player, context);
        } else {
            return AirborneState.checkChildTransition(player, context);
        }

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
