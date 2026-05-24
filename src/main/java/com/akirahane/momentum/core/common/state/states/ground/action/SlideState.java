package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.MovementStateType;
import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;


public class SlideState extends GroundState {
    public SlideState(PlayerMovementContext context) {
        super(context);
    }
    @Override
    public void enter(MovementState previousState, Player player) {
        player.setForcedPose(Pose.SWIMMING);
    }

    /**
     * 离开状态时调用一次
     */
    @Override
    public void exit(MovementState nextState, Player player) {
        player.setForcedPose(null);
    }

    @Override
    public MovementStateType getStateType() {
        return MovementStateType.SLIDE;
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext context) {
        if (!context.isLowerCenter()) {
            return GroundState.newStateCheck(player, nowState, context);
        }
        if (player.getDeltaMovement().horizontalDistance() * 20 < ServerConfig.MIN_SLIDE_SPEED.get()) {
            return ProneState.newStateCheck(player, nowState, context);
        }
        if (!(nowState.getClass() == SlideState.class)) {
            return new SlideState(context);
        }
        return nowState;
    }
}
