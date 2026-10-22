package com.linqiyinya.momentum.core.state.states.ground;

import com.linqiyinya.momentum.client.hud.HintManager;
import com.linqiyinya.momentum.client.hud.WallHangHints;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.linqiyinya.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.linqiyinya.momentum.core.utils.CollisionUtils.canPlayerFitAtPose;
import static com.linqiyinya.momentum.core.state.states.ground.SlideState.canSlideSpeedCheck;


public class ProneState extends BaseState {
    public static boolean canProne(Player player, PlayerMomentumContext context) {
        return (player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) ||
                player.onGround() && checkKey(player, context);
    }
    public static boolean canProneHold(Player player, PlayerMomentumContext context) {
        return (player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) ||
                player.onGround() && checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMomentumContext context) {
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
    public void onEnter(Player player, PlayerMomentumContext context) {
        super.onEnter(player, context);
        Services.PLATFORM.setForcedPose(player, Pose.SWIMMING);
    }

    @Override
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        Services.PLATFORM.setForcedPose(player, null);
    }

    @Override
    public StateType getStateType() {
        return StateType.PRONE;
    }
}
