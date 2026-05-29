package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class WalkState extends BaseState {
    public static boolean canWalk(Player player, PlayerMovementContext context) {
        return player.onGround();
    }

    public static final Identifier FALL_SPEED_PENALTY_ID =
            Identifier.fromNamespaceAndPath("momentum", "fall_speed_penalty");

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
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
