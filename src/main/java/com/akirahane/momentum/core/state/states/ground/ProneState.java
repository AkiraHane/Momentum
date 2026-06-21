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
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.state.states.ground.SlideState.canSlideSpeedCheck;


public class ProneState extends BaseState {
    public static boolean canProne(Player player, PlayerMovementContext context) {
        if (player.onGround() && !canSlideSpeedCheck(player, context) && checkKey(player, context)){
            context.setMomentumProne(true);
            return true;
        } else if (player.getPose() == Pose.SWIMMING){
            context.setMomentumProne(false);
            return true;
        }
        return false;
    }
    public static boolean canProneHold(Player player, PlayerMovementContext context) {
        if (context.isMomentumProne()){
            return player.onGround() && checkKey(player, context);
        } else {
            return player.getPose() == Pose.SWIMMING;
        }
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        if (!HintManager.contains(WallHangHints.VAULT_IN_STAND)){
            HintManager.add(WallHangHints.PRONE);
        }
        return LOWER_CENTER.get().isDown();
    }
    // 状态转换检查
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (OriginalState.canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (VaultInState.canVaultIn(player, context)) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        // 匍匐无法进入滑铲
        if (ProneState.canProneHold(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (VaultUpState.canVaultUp(player, context)) {
            return StateType.VAULT_UP.getState();
        }
        if (WallKickState.canWallKick(player, context)) {
            return StateType.WALL_KICK.getState();
        }
        if (WallHangState.canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (BreakFallReadyState.canBreakFallReady(player, context)) {
            return StateType.BREAK_FALL_READY.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        LOGGER.warn("ProneState evaluate error! 有状态没有覆盖!");
        return super.evaluate(player, context);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        if (context.isMomentumProne()){
            player.setForcedPose(Pose.SWIMMING);
        }
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
