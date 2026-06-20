package com.akirahane.momentum.core.state.states.air;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.MomentumUtils.isDivingEdge;
import static com.akirahane.momentum.core.context.PlayerMovementContext.AIR_LIMIT_ACCELERATION;

public class AirborneState extends BaseState {
    // 动画名称
    public static String FALL = "fall";

    public static boolean canAirborne(Player player, PlayerMovementContext context) {
        return !player.onGround();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        if (!WallKickState.WALL_JUMP_RIGHT.equals(context.getCurrentAnimationName()) &&
                !WallKickState.WALL_JUMP_LEFT.equals(context.getCurrentAnimationName())) {
            super.onEnter(player, context);
        }
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        context.setJumpCooldown(15);
        context.setJumpAnimationSpeed(1F);
        context.setMomentumRollIntensity(8F);

    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        if (player.fallDistance > player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) * 2) {
            float t = (float) ((player.fallDistance - 3.0f) / (70.0f - 3.0f));
            float speed = Math.clamp(t, 0.0f, 1.0f) * 1.5F + 0.5F;
            playStateAnimation(player, FALL, context, 20, speed);
        } else if (FALL.equals(context.getCurrentAnimationName())) {
            playStateAnimation(player, IDLE, context);
        }
        if (WallKickState.WALL_JUMP_RIGHT.equals(context.getCurrentAnimationName()) &&
                WallKickState.WALL_JUMP_LEFT.equals(context.getCurrentAnimationName())) {
            if (context.getSpeed().y > 0) {
                context.setJumpAnimationSpeed(context.getJumpAnimationSpeed() * 0.9F);
            } else {
                context.setJumpAnimationSpeed(context.getJumpAnimationSpeed() * 1.5F);
            }
            playStateAnimation(player, context.getCurrentAnimationName(), context, 0, context.getJumpAnimationSpeed());
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.setMomentumRollIntensity(0);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
    }

    @Override
    public StateType getStateType() {
        return StateType.AIRBORNE;
    }
}
