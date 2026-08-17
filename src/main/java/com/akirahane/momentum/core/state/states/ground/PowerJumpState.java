package com.akirahane.momentum.core.state.states.ground;

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
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class PowerJumpState extends BaseState {

    public static boolean canPowerJump(Player player, PlayerMovementContext context) {
        if (!ServerConfig.ENABLE_POWER_JUMP.getAsBoolean() || !ClientConfig.ENABLE_POWER_JUMP.getAsBoolean()) {
            return false;
        }
        if (!player.onGround()) {
            return false;
        }
        boolean checkRelease = context.wasKeyPressedRecently(SHIFT_HOLD, 5);
        if (checkRelease && Minecraft.getInstance().options.keyShift.isDown()){
            HintManager.add(WallHangHints.POWER_JUMP_READY);
            return false;
        } else if (checkRelease && !Minecraft.getInstance().options.keyShift.isDown()) {
            HintManager.add(WallHangHints.POWER_JUMP);
            return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
        }
        return false;
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.POWER_JUMP_READY);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 蓄力跳持续期自保持（替代 canPowerJump 入口检查）
        return withPredicate(DEFAULT_CHAIN, StateType.POWER_JUMP, (p, c) -> c.getWallJumpTimer() > 0);
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
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        player.setDeltaMovement(
                Mth.abs((float) (player.getDeltaMovement().x + context.getInputVec().x * jumpPower * 0.5)) <
                        Mth.absMax(player.getDeltaMovement().x, context.getInputVec().x * jumpPower * 0.5) ?
                        context.getInputVec().x * jumpPower * 0.5 :
                        player.getDeltaMovement().x + context.getInputVec().x * jumpPower * 0.5
                ,
                jumpPower * 1.5,
                Mth.abs((float) (player.getDeltaMovement().z + context.getInputVec().z * jumpPower * 0.5)) <
                        Mth.absMax(player.getDeltaMovement().z, context.getInputVec().z * jumpPower * 0.5) ?
                        context.getInputVec().z * jumpPower * 0.5 :
                        player.getDeltaMovement().z + context.getInputVec().z * jumpPower * 0.5
        );
        context.playWallSound(player, PlayerMovementContext.FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
        context.setWallJumpTimer(3);
    }

    @Override
    public StateType getStateType() {
        return StateType.POWER_JUMP;
    }
}
