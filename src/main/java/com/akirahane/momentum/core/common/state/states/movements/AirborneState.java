package com.akirahane.momentum.core.common.state.states.movements;

import com.akirahane.momentum.core.common.effect.MomentumEffect;
import com.akirahane.momentum.core.common.effect.MomentumEffectType;
import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.context.PlayerMovementContext;
import com.akirahane.momentum.core.common.state.base.BaseState;
import com.akirahane.momentum.core.common.state.states.MovementState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AirborneState extends MovementState {

    public static MomentumEffect TEMP_ACCELERATION = new MomentumEffect(
            0, 0, 0.1F, 0, -1
    );
    public static MomentumEffect TEMP_ACCELERATION_LIMIT_SPEED = new MomentumEffect(
            0.03F, 0, 1.0F, 0, -1
    );

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.AIR.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION_LIMIT_SPEED).add(TEMP_ACCELERATION_LIMIT_SPEED);
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).add(TEMP_ACCELERATION);

        if (context.getBlockStep() != 0) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(
                    velocity.add(0, velocity.horizontalDistance() * context.getBlockStep(), 0)
            );
            context.setBlockStep(0);
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION_LIMIT_SPEED).remove(TEMP_ACCELERATION_LIMIT_SPEED);
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).remove(TEMP_ACCELERATION);
    }
}
