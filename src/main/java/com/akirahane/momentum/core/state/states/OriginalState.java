package com.akirahane.momentum.core.state.states;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.MomentumUtils.stopAnimation;
import static com.akirahane.momentum.core.context.PlayerMovementContext.DEFAULT_FRICTION;
import static com.akirahane.momentum.core.effect.MomentumEffectType.FRICTION;

public class OriginalState extends BaseState {
    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.AIRBORNE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.resetEffect();
        if (player.level().isClientSide()) {
            stopAnimation((Player) player);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(FRICTION).add(
                DEFAULT_FRICTION
        );
    }

    @Override
    public StateType getStateType() {
        return StateType.ORIGINAL;
    }
}
