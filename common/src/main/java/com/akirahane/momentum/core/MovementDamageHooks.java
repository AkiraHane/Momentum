package com.akirahane.momentum.core;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.platform.PlatformServices;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class MovementDamageHooks {
    private MovementDamageHooks() {
    }

    public static FallDamage adjustFallDamage(Player player, double distance, float multiplier) {
        MovementStateMachine stateMachine = PlatformServices.gameplay().movementState(player);
        double originalDistance = distance;
        float adjustedDistance = (float) distance;

        if (stateMachine.getContext().isHasJetBooster()) {
            adjustedDistance -= 12.0F;
            multiplier *= 0.5F;
        }

        int breakFallReadyCount = stateMachine.getContext().getBreakFallReadyCount();
        if (breakFallReadyCount > 0) {
            adjustedDistance -= 6.0F;
            multiplier *= 0.3F;
            markBreakFallWhenNeeded(player, stateMachine, originalDistance);
        } else if (breakFallReadyCount == 0) {
            adjustedDistance -= 3.0F;
            multiplier *= 0.6F;
            markBreakFallWhenNeeded(player, stateMachine, originalDistance);
        }

        return new FallDamage(Math.max(0.0F, adjustedDistance), multiplier);
    }

    public static boolean shouldCancelDamage(Player player) {
        return StateType.DODGE.equals(
                PlatformServices.gameplay().movementState(player).getCurrentState().getStateType());
    }

    private static void markBreakFallWhenNeeded(
            Player player, MovementStateMachine stateMachine, double originalDistance) {
        if (originalDistance > Math.min(4.0, player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE))) {
            stateMachine.getContext().setToBreakFallState(true);
        }
    }

    public record FallDamage(double distance, float multiplier) {
    }
}
