package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WallHangState extends BaseState {

    public static boolean canWallHang(Player player, PlayerMovementContext context) {
        return context.isHasLedge() &&
                !Minecraft.getInstance().options.keyJump.isDown();

    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (context.isLowerCenter() && AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (context.isLowerCenter() && WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_SLIDE.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_HANG;
    }
}
