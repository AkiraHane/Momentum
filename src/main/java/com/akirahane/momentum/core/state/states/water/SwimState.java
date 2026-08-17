package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.PowerJumpState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;

public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        if (player.isSwimming()) {
            return true;
        } else if (player.isUnderWater()) {
            if (player.isSprinting()) {
                HintManager.add(WallHangHints.SWIM_HOLD);
                return Minecraft.getInstance().options.keyUp.isDown();
            } else {
                HintManager.add(WallHangHints.SWIM);
                return false;
            }
        } else if (player.isInWater()) {
            HintManager.add(WallHangHints.SWIM_ACTIVE);
            return LOWER_CENTER.get().isDown() && Minecraft.getInstance().options.keyUp.isDown();
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
