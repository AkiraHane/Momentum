package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.platform.PlatformServices;
import com.akirahane.momentum.platform.client.MovementHint;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.state.states.ground.SlideState.canSlideSpeedCheck;


public class ProneState extends BaseState {
    public static boolean canProne(Player player, PlayerMovementContext context) {
        return (player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) ||
                player.onGround() && checkKey(player, context);
    }
    public static boolean canProneHold(Player player, PlayerMovementContext context) {
        return (player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) ||
                player.onGround() && checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        if (!canSlideSpeedCheck(player, context) && !PlatformServices.client().containsHint(MovementHint.VAULT_IN_STAND)) {
            PlatformServices.client().addHint(MovementHint.PRONE);
        }
        return context.getMovementInput().lower();
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 匍匐无法进入滑铲；进入检查换成更宽松的自保持检查
        return withPredicate(
                without(DEFAULT_CHAIN, StateType.SLIDE),
                StateType.PRONE, ProneState::canProneHold);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        PlatformServices.gameplay().setForcedPose(player, Pose.SWIMMING);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        PlatformServices.gameplay().setForcedPose(player, null);
    }

    @Override
    public StateType getStateType() {
        return StateType.PRONE;
    }
}
