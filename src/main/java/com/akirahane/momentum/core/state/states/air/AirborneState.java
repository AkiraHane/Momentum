package com.akirahane.momentum.core.state.states.air;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.PendingEffect;
import com.akirahane.momentum.core.enumerate.MomentumEffectType;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.player.Player;

public class AirborneState extends BaseState {
    public static boolean canAirborne(Player player, PlayerMovementContext context) {
        return !player.onGround();
    }

    public static PendingEffect AIR_ACCELERATION = new PendingEffect(
            0, 0, 0.1F, 0, -1
    );

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (player.onGround() && context.isLowerCenter()) {
            return StateType.PRONE.getState();
        }
        if (player.onGround() && !context.isLowerCenter()) {
            return StateType.WALK.getState();
        }
        return StateType.AIRBORNE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).add(AIR_ACCELERATION);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).remove(AIR_ACCELERATION);
    }

    @Override
    public StateType getStateType() {
        return StateType.AIRBORNE;
    }
}
