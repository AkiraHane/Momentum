package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class VaultInState extends BaseState {
    // 动画名称
    public static final String VAULT_IN = "vault_in";

    public static boolean canVaultIn(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_VAULT_IN.getAsBoolean() && ClientConfig.ENABLE_VAULT_IN.getAsBoolean() &&
                (context.isHasLedge() || player.onGround() && !context.isHasFaceWall()) &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 90 &&
                checkKey(player, context)
                ;
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        if (player.onGround() && !context.isHasFaceWall()){
            HintManager.add(WallHangHints.VAULT_IN_STAND);
            return Minecraft.getInstance().options.keyUp.isDown() &&
                    LOWER_CENTER.get().isDown()
                    ;
        } else {
            HintManager.add(WallHangHints.VAULT_IN);
            return LOWER_CENTER.get().isDown() &&
                    Minecraft.getInstance().options.keyJump.isDown();
        }
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setVaultTimer(10);
        context.setNoJump(true);
        if (player.onGround()){
            var instance = player.getAttribute(Attributes.STEP_HEIGHT);
            if (instance != null && instance.getModifier(UP_SLOPE_ID) == null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        UP_SLOPE_ID,
                        0.6,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        } else {
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    0.6,
                    player.getDeltaMovement().z
            );
        }
        playStateAnimation(player, VAULT_IN, context, 2, 1.5F);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        var instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.removeModifier(UP_SLOPE_ID);
        }
        player.setForcedPose(null);
        context.setNoJump(false);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimDashState.canSwimDash(player, context)){
            return StateType.SWIM_DASH.getState();
        }
        if (context.getVaultTimer() > 0) {
            return StateType.VAULT_IN.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
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
        // 上翻不需要再这个时候运行
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
        return StateType.VAULT_IN;
    }
}
