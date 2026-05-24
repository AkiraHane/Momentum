package com.akirahane.momentum.core.common.state.states.ground;

import com.akirahane.momentum.core.common.state.MovementStateType;
import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.action.ProneState;
import com.akirahane.momentum.core.common.state.states.ground.action.SlideState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

public class GroundState extends MovementState {
    public GroundState(PlayerMovementContext context) {
        super(context);
    }

    @Override
    public MovementState toStateCheck(Player player, MovementState nowState) {
        MovementState needState = super.toStateCheck(player, nowState);
        if (!needState.getClass().isInstance(this)) {
            return needState;
        }
        return newStateCheck(player, nowState, context);
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext context) {
        LOGGER.debug("horizontalDistance: {}", player.getDeltaMovement().horizontalDistance() * 20);
        if (context.isLowerCenter() && player.getDeltaMovement().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get()) {
            return ProneState.newStateCheck(player, nowState, context);
        }
        if (context.isLowerCenter() && player.getDeltaMovement().horizontalDistance() * 20 > ServerConfig.MIN_SLIDE_SPEED.get()) {
            return SlideState.newStateCheck(player, nowState, context);
        }
        if (!(nowState.getClass() == GroundState.class)) {
            return new GroundState(context);
        }
        return nowState;
    }

    @Override
    public MovementStateType getStateType() {
        return MovementStateType.GROUND;
    }
}
