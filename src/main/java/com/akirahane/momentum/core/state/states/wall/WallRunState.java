package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class WallRunState extends BaseState {
    public static boolean canWallRun(LocalPlayer player, PlayerMovementContext context) {
        return !player.onClimbable() &&
                context.isHasJetBooster() &&
                context.getInputWallAngle() < 90 &&
                context.getLookWallAngle() > 45 &&
                Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_SLIDE.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_RUN;
    }
}
