package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class BreakFallState extends BaseState {
    // 动画名称
    public static String BREAK_FALL = "break_fall";

    public static boolean canBreakFall(Player player, PlayerMovementContext context) {
        return context.isToBreakFallState() && context.getLastFallDistance() > 4;
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setBreakFallTimer(12);
        context.setNoJump(true);
        context.setNoMoveInput(true);
        playStateAnimation(player, BREAK_FALL, context, 0, 2f);
        Vec3 direction = Vec3.directionFromRotation(0, player.getYRot());
        player.setDeltaMovement(direction.x, 0.0D, direction.z);
        context.addEffect(MomentumEffectType.FRICTION, context.BREAK_FALL_FRICTION, 6);

    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.setNoJump(false);
        context.setNoMoveInput(false);
        context.removeEffect(MomentumEffectType.FRICTION, context.BREAK_FALL_FRICTION);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (context.getBreakFallTimer() > 0) {
            return StateType.BREAK_FALL.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.BREAK_FALL.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL;
    }
}
