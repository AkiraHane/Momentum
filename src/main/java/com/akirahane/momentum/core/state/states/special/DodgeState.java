package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.wall.*;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.init.InitSounds.JET2;
import static com.akirahane.momentum.config.ServerConfig.DODGE_COOLDOWN;
import static com.akirahane.momentum.config.ServerConfig.DODGE_STORAGE;
import static com.akirahane.momentum.core.MomentumUtils.canPlayerFitAtPose;
import static com.akirahane.momentum.core.context.PlayerMovementContext.SPRINT;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class DodgeState extends BaseState {
    // 动画名称
    public static String DODGE_UP = "dodge_up";
    public static String DODGE_DOWN = "dodge_down";
    public static String DODGE_LEFT = "dodge_left";
    public static String DODGE_RIGHT = "dodge_right";

    public static boolean canDodge(Player player, PlayerMovementContext context) {
        if (!ServerConfig.ENABLE_DODGE.getAsBoolean() || !ClientConfig.ENABLE_DODGE.getAsBoolean()) {
            return false;
        }
        if (!player.onGround() && !context.isHasJetBooster() && !player.isSwimming()) {
            return false;
        }
        if (!player.isSwimming() && (player.getPose() == Pose.SWIMMING) && !canPlayerFitAtPose(player, Pose.CROUCHING)) {
            return false;
        }
        if (DODGE_COOLDOWN.get() * DODGE_STORAGE.get() - context.getDodgeCooldown() <= DODGE_COOLDOWN.get()) {
            return false;
        }
        if (player.isSwimming()) {
            HintManager.add(WallHangHints.PUSH);
            return context.getInputBuffer()[context.getInputBufferIndex()].contains(SPRINT);
        }
        HintManager.add(WallHangHints.DODGE);
        return Minecraft.getInstance().options.keySprint.isDown() &&
                (context.isDoubleClickUp() || context.isDoubleClickDown() || context.isDoubleClickLeft() || context.isDoubleClickRight());
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        String animationName;
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        if (context.isDoubleClickDown()) {
            yRot += 180;
            animationName = DODGE_DOWN;
        } else if (context.isDoubleClickLeft()) {
            yRot -= 90;
            animationName = DODGE_LEFT;
        } else if (context.isDoubleClickRight()) {
            yRot += 90;
            animationName = DODGE_RIGHT;
        } else {
            animationName = DODGE_UP;
        }
        Vec3 direction;
        if (!player.isSwimming()){
            direction = Vec3.directionFromRotation(4, yRot);
            playStateAnimation(player, animationName, context, 4, 2f);
        } else {
            direction = Vec3.directionFromRotation(xRot, yRot);
        }
        if (!context.isHasJetBooster() && !player.isSwimming()) {
            player.setDeltaMovement(direction.x * 0.8, player.getDeltaMovement().y, direction.z * 0.8);
        } else {
            double currentSpeed = player.getDeltaMovement().horizontalDistance();
            double targetSpeed = direction.horizontalDistance() * 0.8;
            if (currentSpeed > targetSpeed) {
                // 根据转向角度衰减速度
                Vec3 currentHorizontal = player.getDeltaMovement().multiply(1, 0, 1).normalize();
                Vec3 targetHorizontal = new Vec3(direction.x, 0, direction.z).normalize();
                double dot = currentHorizontal.dot(targetHorizontal); // 1=同向, 0=90度, -1=反向
                double factor = Math.max(0, dot); // 90度以上直接归零

                double finalSpeed = currentSpeed * factor;

                if (finalSpeed < targetSpeed) {
                    finalSpeed = targetSpeed;
                }

                Vec3 normalized = new Vec3(
                        direction.x,
                        !player.isSwimming() ? 0.3F : direction.y,
                        direction.z
                ).normalize();
                player.setDeltaMovement(normalized.x * finalSpeed, normalized.y, normalized.z * finalSpeed);
            } else {
                player.setDeltaMovement(
                        direction.x * 0.8,
                        !player.isSwimming() ? 0.3F : direction.y,
                        direction.z * 0.8);
            }
            player.fallDistance = 0;
        }
        context.setDodgeTimer(8);
        context.setDodgeCooldown(context.getDodgeCooldown() + DODGE_COOLDOWN.get());
        context.setNoJump(true);
        context.setNoMoveInput(true);
        context.addEffect(MomentumEffectType.BLOCK_FRICTION, context.DODGE_BLOCK_FRICTION, 3);
        if (context.isHasJetBooster()) {
            player.playSound(JET2.value(), 1F, 1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F);
        } else if (!player.isSwimming()){
            player.playSound(
                    SoundEvents.ARROW_SHOOT,
                    0.5F,
                    1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
            );
        }
        if (player.isSwimming()){
            // 入水的声音
            player.playSound(
                    SoundEvents.PLAYER_SWIM,
                    0.5F,
                    1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F
            );
        }
        context.setMomentumRollIntensity(20F);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        if (context.getDodgeTimer() > 6) {
            context.setMomentumRollIntensity(0F);
            if (!player.onGround() && !player.isSwimming()) {
                playStateAnimation(player, IDLE, context);
            }
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
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (context.getDodgeTimer() > 0) {
            return StateType.DODGE.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (VaultInState.canVaultIn(player, context)) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallKickState.canWallKick(player, context)) {
            return StateType.WALL_KICK.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (VaultUpState.canVaultUp(player, context)) {
            return StateType.VAULT_UP.getState();
        }
        if (WallHangState.canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (BreakFallReadyState.canBreakFallReady(player, context)) {
            return StateType.BREAK_FALL_READY.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        LOGGER.warn("evaluate error! 有状态没有覆盖!");
        return super.evaluate(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.DODGE;
    }
}
