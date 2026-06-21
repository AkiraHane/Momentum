package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class VaultUpState extends BaseState {

    public static boolean canVaultUp(Player player, PlayerMovementContext context) {
        return context.isHasLedge() &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 90 &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.VAULT_UP);
        return Minecraft.getInstance().options.keyJump.isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        context.setVaultTimer(6);
        player.setDeltaMovement(
                0,
                0.6,
                0
        );
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
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (context.getVaultTimer() > 0) {
            return StateType.VAULT_UP.getState();
        }
        return super.evaluate(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT_UP;
    }
}
