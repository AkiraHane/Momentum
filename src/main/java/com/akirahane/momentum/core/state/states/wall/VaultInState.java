package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class VaultInState extends BaseState {
    // 动画名称
    public static final String VAULT_IN = "vault_in";

    public static boolean canVaultIn(Player player, PlayerMovementContext context) {
        return context.isHasLedge() &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 90 &&
                checkKey(player, context)
                ;
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.VAULT_IN);
        return LOWER_CENTER.get().isDown() &&
                Minecraft.getInstance().options.keyUp.isDown() &&
                Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setVaultTimer(10);
        player.setDeltaMovement(
                0,
                0.6,
                0
        );
        playStateAnimation(player, VAULT_IN, context, 2, 1.5F);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        player.setForcedPose(null);
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (context.getVaultTimer() > 0) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        return StateType.VAULT_IN.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT_IN;
    }
}
