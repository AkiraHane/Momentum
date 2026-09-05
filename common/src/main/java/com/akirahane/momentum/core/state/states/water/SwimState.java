package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.platform.PlatformServices;
import com.akirahane.momentum.platform.client.MovementHint;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.world.entity.player.Player;


public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        if (player.isSwimming()) {
            return true;
        } else if (player.isUnderWater()) {
            if (player.isSprinting()) {
                PlatformServices.client().addHint(MovementHint.SWIM_HOLD);
                return context.getMovementInput().up();
            } else {
                PlatformServices.client().addHint(MovementHint.SWIM);
                return false;
            }
        } else if (player.isInWater()) {
            PlatformServices.client().addHint(MovementHint.SWIM_ACTIVE);
            return context.getMovementInput().lower() && context.getMovementInput().up();
        }
        return false;
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 游泳状态下不能滑铲
        return without(DEFAULT_CHAIN, StateType.SLIDE);
    }

    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.trace("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
