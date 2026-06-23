package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;
import static net.minecraft.world.level.block.SoundType.*;

public class WallSlideState extends BaseState {
    // 动画名称
    public static String WALL_SLIDE = "wall_slide";

    public static boolean canWallSlide(Player player, PlayerMovementContext context) {
        if (!ServerConfig.ENABLE_WALL_SLIDE.getAsBoolean() || !ClientConfig.ENABLE_WALL_SLIDE.getAsBoolean()){
            return false;
        }
        if (player.onClimbable()){
            HintManager.add(WallHangHints.CLIMB_ACCELERATION);
        }
        return !player.onGround() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                (player.onClimbable() ||
                        !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 30 ||
                        checkKey(player, context)
                ) &&
                context.getSpeed().y < 0 &&
                Mth.abs(context.getLookWallAngle()) < 30;
    }

    // 维持
    public static boolean canWallSlideHold(Player player, PlayerMovementContext context) {
        if (!ServerConfig.ENABLE_WALL_SLIDE.getAsBoolean() || !ClientConfig.ENABLE_WALL_SLIDE.getAsBoolean()){
            return false;
        }
        if (player.onClimbable()){
            HintManager.add(WallHangHints.CLIMB_ACCELERATION);
        }
        return !player.onGround() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                (player.onClimbable() ||
                        !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 60 ||
                        checkKey(player, context)
                ) &&
                Mth.abs(context.getLookWallAngle()) < 60;
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_SLIDE);
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
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
        if (WallClimbState.canWallClimbHold(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlideHold(player, context)) {
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
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        playStateAnimation(player, WALL_SLIDE, context, 4, 1);
        context.setTargetArmTransform(-0.15F, 5F);
    }

    @Override
    public void serverTick(Player player, PlayerMovementContext context) {
        super.serverTick(player, context);
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (context.getSpeed().y > 0 && instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        } else if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -0.9,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        if (player.tickCount % 2 == 0) {
            player.playSound(
                    GRASS.getStepSound(),
                    0.05F,
                    1F
            );
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_SLIDE;
    }
}
