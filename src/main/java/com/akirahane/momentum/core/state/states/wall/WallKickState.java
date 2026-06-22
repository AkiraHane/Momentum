package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class WallKickState extends BaseState {
    // 跳跃
    public static String WALL_JUMP_LEFT = "wall_jump_left";
    public static String WALL_JUMP_RIGHT = "wall_jump_right";

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
        if (OriginalState.canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (DodgeState.canDodge(player, context)) {
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
        if (context.getWallJumpTimer() > 0) {
            return StateType.WALL_KICK.getState();
        }
        return super.evaluate(player, context);
    }


    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        if (context.isLeftFootJump()) {
            playStateAnimation(player, WALL_JUMP_LEFT, context);
        } else {
            playStateAnimation(player, WALL_JUMP_RIGHT, context);
        }
        context.setLeftFootJump(!context.isLeftFootJump());
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        if (Mth.abs(context.getInputWallAngle()) >= 100) {
            player.addDeltaMovement(
                    new Vec3(
                            context.getInputVec().x * jumpPower * 0.5,
                            jumpPower,
                            context.getInputVec().z * jumpPower * 0.5
                    )
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
        context.playWallSound(player, FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
        context.setWallJumpTimer(5);
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
