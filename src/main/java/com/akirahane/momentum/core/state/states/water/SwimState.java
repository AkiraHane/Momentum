package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping keyUp = mc.options.keyUp;
        Float xRot = null;
        if (mc.player != null) {
            xRot = mc.player.getXRot(); // 垂直角度 (pitch), -90 到 90
        }
        return context.isLowerCenter() && player.isInWater() && keyUp.isDown() && xRot != null && xRot > 0;
    }

    public void onEnter(Player player, PlayerMovementContext context) {
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.debug("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMovementContext context) {
        player.setSwimming(false);
        player.setSprinting(false);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (player.isSwimming()) {
            return StateType.SWIM.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.SWIM.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
