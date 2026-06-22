package com.akirahane.momentum.core.state.states.air;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.wall.WallKickState;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.context.PlayerMovementContext.AIR_LIMIT_ACCELERATION;
import static com.akirahane.momentum.core.state.states.air.AirborneState.FALL;

public class BreakFallReadyState extends BaseState {
    // 动画名称
    public static String BREAK_FALL_READY = "break_fall_ready";

    public static boolean canBreakFallReady(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_BREAK_FALL_READY.getAsBoolean() && ClientConfig.ENABLE_BREAK_FALL_READY.getAsBoolean() &&
                !player.onGround() &&
                player.fallDistance > Math.min(4, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE)) &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        if (context.getLuckyNumber() > 98){
            HintManager.add(WallHangHints.BREAK_FALL_READY_EGG);
        } else {
            HintManager.add(WallHangHints.BREAK_FALL_READY);
        }
        return LOWER_CENTER.get().isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.setLuckyNumber(player);
        if (!WallKickState.WALL_JUMP_RIGHT.equals(context.getCurrentAnimationName()) &&
                !WallKickState.WALL_JUMP_LEFT.equals(context.getCurrentAnimationName())
        ) {
            super.onEnter(player, context);
        }
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        context.setJumpCooldown(15);
        context.setJumpAnimationSpeed(1F);
        context.setMomentumRollIntensity(8F);
        context.setBreakFallReadyCount(6);
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
        context.setBreakFallReadyCount(-1);
        context.setToBreakFallState(false);
        context.setMomentumRollIntensity(0);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
    }

    @Override
    public StateType getStateType() {
        return StateType.BREAK_FALL_READY;
    }
}
