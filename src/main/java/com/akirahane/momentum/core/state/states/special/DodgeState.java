package com.akirahane.momentum.core.state.states.special;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DodgeState extends BaseState {
    public static boolean canDodge(LocalPlayer player, PlayerMovementContext context) {
        Minecraft mc = Minecraft.getInstance();
        KeyMapping keyShift = mc.options.keyShift;
        return player.onGround() &&
                !player.isInLiquid() &&
                keyShift.isDown() &&
                (context.isDoubleClickUp() || context.isDoubleClickDown() || context.isDoubleClickLeft() || context.isDoubleClickRight());
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        Vec3 direction = Vec3.directionFromRotation(player.getXRot(), player.getYRot());
        player.setDeltaMovement(direction.x, 0.0D, direction.z);
        context.setDodgeTimer(10);
        context.setNoJump(true);
        context.setNoMoveInput(true);
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.setNoJump(false);
        context.setNoMoveInput(false);
    }

    @Override
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        BaseState baseEvaluate = super.evaluate(player, context);
        if (baseEvaluate != null) {
            return baseEvaluate;
        }
        if (context.getDodgeTimer() > 0) {
            return StateType.DODGE.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.DODGE.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.DODGE;
    }
}
