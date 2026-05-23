package com.akirahane.momentum.core.common.state.states.ground;

import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.action.CrouchState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.init.ModKeyMappings.LOWER_CENTER_KEY_MAPPING;

public class GroundState extends MovementState {
    public GroundState(PlayerMovementContext data) {
        super(data);
    }

    @Override
    public MovementState toStateCheck(Player player, MovementState nowState) {
        MovementState needState = super.toStateCheck(player, nowState);
        if (!needState.getClass().isInstance(nowState)) {
            return needState;
        }
        return newStateCheck(player, nowState, data);
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext data) {
        if (LOWER_CENTER_KEY_MAPPING.get().isDown() && data.getVelocity().horizontalDistance() < ServerConfig.MIN_SLIDE_SPEED.get()) {
            return CrouchState.newStateCheck(player, nowState, data);
        }
        if (!(nowState.getClass() == GroundState.class)) {
            return new GroundState(data);
        }
        return nowState;
    }
}
