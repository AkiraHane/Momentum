package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.WallClimbState;
import com.akirahane.momentum.core.state.states.wall.WallSlideState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WalkState extends BaseState {
    public static boolean canWalk(Player player, PlayerMovementContext context) {
        return player.onGround();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
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
