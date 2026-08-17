package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
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
        if (!canSlideSpeedCheck(player, context) && !HintManager.contains(WallHangHints.VAULT_IN_STAND)) {
            HintManager.add(WallHangHints.PRONE);
        }
        return LOWER_CENTER.get().isDown();
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
        player.setForcedPose(Pose.SWIMMING);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        player.setForcedPose(null);
    }

    @Override
    public StateType getStateType() {
        return StateType.PRONE;
    }
}
