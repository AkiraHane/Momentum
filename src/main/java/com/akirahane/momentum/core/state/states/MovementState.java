package com.akirahane.momentum.core.state.states;

import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.AirState;
import com.akirahane.momentum.core.state.states.movements.GroundState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.movements.WaterState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.Momentum.MODID;

public abstract class MovementState extends State {

    public static final AttributeModifier SLIDE_STEP_HEIGHT = new AttributeModifier(
            Identifier.fromNamespaceAndPath(MODID, "slide_step_height"),
            ServerConfig.ADD_AUTO_CLIMB_HEIGHT.get(),
            AttributeModifier.Operation.ADD_VALUE
    );

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        if (player.isSwimming() || (player.isInWater() && nowState.getStateType() != StateType.SLIDE && context.isLowerCenter())) {
            return WaterState.checkChildTransition(player, context, nowState);
        }
        if (player.onGround() || nowState.getStateType() == StateType.SLIDE) {
            return GroundState.checkChildTransition(player, context, nowState);
        }
        return AirState.checkChildTransition(player, context, nowState);

    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        // AttributeInstance有服务器到客户端的同步机制
        // 如果客户端也跑, 有概率会出现服务器同步过来后再次设置导致报错
        if (player instanceof ServerPlayer) {
            AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                // TODO 正式版本改为使用addOrUpdateTransientModifier, 测试和开发要验证状态机和防止BUG, 不对可能的错误进行处理
//                attr.addOrUpdateTransientModifier(SLIDE_STEP_HEIGHT);
                attr.addTransientModifier(SLIDE_STEP_HEIGHT);
            }
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        if (player instanceof ServerPlayer) {
            AttributeInstance attr = player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                attr.removeModifier(SLIDE_STEP_HEIGHT);
            }
        }
    }
}
