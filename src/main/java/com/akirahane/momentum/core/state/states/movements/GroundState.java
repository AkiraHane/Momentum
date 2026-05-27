package com.akirahane.momentum.core.state.states.movements;

import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.MovementState;
import com.akirahane.momentum.core.state.states.movements.ground.ProneState;
import com.akirahane.momentum.core.state.states.movements.ground.SlideState;
import com.akirahane.momentum.core.state.states.movements.ground.WalkState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

public abstract class GroundState extends MovementState {
    public static MomentumEffect JUMP_ACCELERATION_LIMIT_SPEED = new MomentumEffect(
            0.03F, 0, 1.0F, 0, -1
    );

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        // 输出速度
        if (context.isLowerCenter() &&
                context.getSpeed().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get() &&
                2 * context.getSpeed().horizontalDistance() > Math.abs(context.getSpeed().y) &&
                nowState.getStateType() != StateType.PRONE
        ) {
            return SlideState.checkChildTransition(player, context, nowState);
        }
        if (context.isLowerCenter()) {
            return ProneState.checkChildTransition(player, context, nowState);
        }
        return WalkState.checkChildTransition(player, context, nowState);
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }
}
