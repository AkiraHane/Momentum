package com.akirahane.momentum.core.state.states.movements.air;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.AirState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AirborneState extends AirState {

    public static MomentumEffect TEMP_ACCELERATION = new MomentumEffect(
            0, 0, 0.1F, 0, -1
    );
    public static MomentumEffect TEMP_ACCELERATION_LIMIT_SPEED = new MomentumEffect(
            0.03F, 0, 1.0F, 0, -1
    );

    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.AIRBORNE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION_LIMIT_SPEED).add(TEMP_ACCELERATION_LIMIT_SPEED);
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).add(TEMP_ACCELERATION);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION_LIMIT_SPEED).remove(TEMP_ACCELERATION_LIMIT_SPEED);
        context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).remove(TEMP_ACCELERATION);
    }
}
