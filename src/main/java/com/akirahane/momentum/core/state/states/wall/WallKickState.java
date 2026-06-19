package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.JUMP;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WallKickState extends BaseState {
    // 跳跃
    public static String WALL_JUMP_LEFT = "wall_jump_left";
    public static String WALL_JUMP_RIGHT = "wall_jump_right";

    public static boolean canWallKick(Player player, PlayerMovementContext context) {
        return context.getWallDirection() != null &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) >= 90 &&
                context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        if (context.isLeftFootJump()) {
            playStateAnimation(player, WALL_JUMP_LEFT, context);
        } else {
            playStateAnimation(player, WALL_JUMP_RIGHT, context);
        }
        context.setLeftFootJump(!context.isLeftFootJump());
        player.addDeltaMovement(
                new Vec3(
                        context.getInputVec().x * 0.3,
                        0.6,
                        context.getInputVec().z * 0.3
                )
        );
    }
    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
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
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_KICK.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
