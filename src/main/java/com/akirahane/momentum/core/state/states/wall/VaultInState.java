package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.special.DodgeState;
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
        var instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null && instance.getModifier(UP_SLOPE_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    UP_SLOPE_ID,
                    0.6,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
        playStateAnimation(player, VAULT_IN, context, 2, 1.5F);
        context.setMomentumProne(true);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        var instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.removeModifier(UP_SLOPE_ID);
        }
        player.setForcedPose(null);
        context.setMomentumProne(false);
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
            return StateType.VAULT_IN.getState();
        }
        return super.evaluate(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT_IN;
    }
}
