package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;

public class SwimDashState extends BaseState {

    public static boolean canSwim(Player player, PlayerMovementContext context) {
        if (player.isSwimming()) {
            return true;
        } else if (player.isUnderWater()) {
            if (player.isSprinting()) {
                HintManager.add(WallHangHints.SWIM_HOLD);
                return Minecraft.getInstance().options.keyUp.isDown();
            } else {
                HintManager.add(WallHangHints.SWIM);
                return false;
            }
        } else if (player.isInWater()) {
            HintManager.add(WallHangHints.SWIM_ACTIVE);
            return LOWER_CENTER.get().isDown() && Minecraft.getInstance().options.keyUp.isDown();
        }
        return false;
    }

    @Override
    // 状态转换检查
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
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (VaultInState.canVaultIn(player, context)) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimDashState.canSwim(player, context)) {
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

    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.trace("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
//        player.setSwimming(false);
//        player.setSprinting(false);
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
