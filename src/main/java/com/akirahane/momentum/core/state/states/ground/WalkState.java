package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.WallRunState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class WalkState extends BaseState {
    public static boolean canWalk(LocalPlayer player, PlayerMovementContext context) {
        return player.onGround();
    }

    // TODO 掉落移速惩罚
    // TODO 地面状态处于水中时, 根据水面占比碰撞箱调整减速

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
    }

    @Override
    public void serverTick(Player player, PlayerMovementContext context) {
        super.serverTick(player, context);
    }

    @Override
    public void clientTick(LocalPlayer player, PlayerMovementContext context) {
        super.clientTick(player, context);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
    }

    @Override
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        return StateType.WALK.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALK;
    }
}
