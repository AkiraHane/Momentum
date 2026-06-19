package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;


public class ProneState extends BaseState {
    public static boolean canProne(Player player, PlayerMovementContext context) {
        return player.onGround() && LOWER_CENTER.get().isDown() || (
                        player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)
                );
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.PRONE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        player.setForcedPose(Pose.SWIMMING);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
    }

    @Override
    public StateType getStateType() {
        return StateType.PRONE;
    }
}
