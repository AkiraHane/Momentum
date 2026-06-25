package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class WallKickState extends BaseState {

    public static boolean canWallKick(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                context.getInputVec().horizontalDistance() > 0.01 && Mth.abs(context.getInputWallAngle()) >= 100 &&
                checkKey(player, context);
    }

    // 跑墙特殊进入条件
    public static boolean canWallKickRun(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_WALL_KICK.getAsBoolean() && ClientConfig.ENABLE_WALL_KICK.getAsBoolean() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_KICK);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (OriginalState.canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimDashState.canSwimDash(player, context)){
            return StateType.SWIM_DASH.getState();
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
        if (context.getWallJumpTimer() > 0) {
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
    public void onEnter(Player player, PlayerMovementContext context) {
        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        ).normalize();
        Vec3 motionDirection = context.getSpeed().normalize();
        boolean isBackwardJump = (lookVec.x * motionDirection.x + lookVec.z * motionDirection.z) < 0;
        if (context.isLeftFootJump()) {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_LEFT : BaseState.JUMP_LEFT,
                    context);
        } else {
            playStateAnimation(player,
                    isBackwardJump ? BaseState.BACK_JUMP_RIGHT : BaseState.JUMP_RIGHT,
                    context);
        }
        context.setLeftFootJump(!context.isLeftFootJump());
        context.setLeftFootJump(!context.isLeftFootJump());
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        if (Mth.abs(context.getInputWallAngle()) >= 100) {
            // 加速倍率
            float limitJumpPower = jumpPower;
            if (context.getWallKickCooldown() != 0) {
                limitJumpPower = 0;
            }
            player.setDeltaMovement(
                    Mth.abs((float) (player.getDeltaMovement().x + context.getInputVec().x * limitJumpPower * 0.5)) <
                            Mth.absMax(player.getDeltaMovement().x, context.getInputVec().x * jumpPower * 0.5) ?
                            context.getInputVec().x * jumpPower * 0.5 :
                            player.getDeltaMovement().x + context.getInputVec().x * limitJumpPower * 0.5
                    ,
                    jumpPower * 1.5,
                    Mth.abs((float) (player.getDeltaMovement().z + context.getInputVec().z * limitJumpPower * 0.5)) <
                            Mth.absMax(player.getDeltaMovement().z, context.getInputVec().z * jumpPower * 0.5) ?
                            context.getInputVec().z * jumpPower * 0.5 :
                            player.getDeltaMovement().z + context.getInputVec().z * limitJumpPower * 0.5
            );
            player.fallDistance = 0;
        } else {
            player.addDeltaMovement(
                    new Vec3(
                            -context.getWallNormal().x * jumpPower,
                            0,
                            -context.getWallNormal().z * jumpPower
                    )
            );
        }
        context.playWallSound(player, PlayerMovementContext.FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
        context.setWallJumpTimer(5);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.setWallKickCooldown(ServerConfig.WALL_KICK_ACCELERATION_COOLDOWN.getAsInt());
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
