package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.state.states.ground.SlideState.canSlideSpeedCheck;


public class ProneState extends BaseState {
    public static boolean canProne(Player player, PlayerMovementContext context) {
        return (player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) ||
                player.onGround() && !canSlideSpeedCheck(player, context) && checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.PRONE);
        return LOWER_CENTER.get().isDown();
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
