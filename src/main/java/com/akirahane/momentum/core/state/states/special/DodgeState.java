package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.config.ServerConfig.DODGE_COOLDOWN;
import static com.akirahane.momentum.config.ServerConfig.DODGE_STORAGE;
import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class DodgeState extends BaseState {
    // 动画名称
    public static String DODGE_UP = "dodge_up";
    public static String DODGE_DOWN = "dodge_down";
    public static String DODGE_LEFT = "dodge_left";
    public static String DODGE_RIGHT = "dodge_right";

    public static boolean canDodge(Player player, PlayerMovementContext context) {
        return (player.onGround() || context.isHasJetBooster()) &&
                !(player.getPose() == Pose.SWIMMING && !canPlayerFitAtPose(player, Pose.CROUCHING)) &&
                DODGE_COOLDOWN.get() * DODGE_STORAGE.get() - context.getDodgeCooldown() > DODGE_COOLDOWN.get() &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.DODGE);
        return Minecraft.getInstance().options.keySprint.isDown() &&
                (context.isDoubleClickUp() || context.isDoubleClickDown() || context.isDoubleClickLeft() || context.isDoubleClickRight());
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        float yRot = player.getYRot();
        if (context.isDoubleClickDown()) {
            yRot += 180;
            playStateAnimation(player, DODGE_DOWN, context, 4, 2f);
        } else if (context.isDoubleClickLeft()) {
            yRot -= 90;
            playStateAnimation(player, DODGE_LEFT, context, 4, 2f);
        } else if (context.isDoubleClickRight()) {
            yRot += 90;
            playStateAnimation(player, DODGE_RIGHT, context, 4, 2f);
        } else {
            playStateAnimation(player, DODGE_UP, context, 4, 2f);
        }
        Vec3 direction = Vec3.directionFromRotation(4, yRot);
        if (!context.isHasJetBooster()){
            player.setDeltaMovement(direction.x * 0.8, player.getDeltaMovement().y, direction.z * 0.8);
        } else {
            player.setDeltaMovement(direction.x * 0.8, 0.3F, direction.z * 0.8);
            player.fallDistance = 0;
        }
        context.setDodgeTimer(8);
        context.setDodgeCooldown(context.getDodgeCooldown() + DODGE_COOLDOWN.get());
        context.setNoJump(true);
        context.setNoMoveInput(true);
        context.addEffect(MomentumEffectType.BLOCK_FRICTION, context.DODGE_BLOCK_FRICTION, 3);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        if (context.getDodgeTimer() > 6 && !player.onGround()) {
            context.setDodgeTimer(0);
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.setNoJump(false);
        context.setNoMoveInput(false);
        context.removeEffect(MomentumEffectType.BLOCK_FRICTION, context.DODGE_BLOCK_FRICTION);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (context.getDodgeTimer() > 0) {
            return StateType.DODGE.getState();
        }
        return super.evaluate(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.DODGE;
    }
}
