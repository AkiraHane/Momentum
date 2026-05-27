package com.akirahane.momentum.core.common.state.states.movements.grounds;

import com.akirahane.momentum.core.common.effect.MomentumEffect;
import com.akirahane.momentum.core.common.effect.MomentumEffectType;
import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.movements.GroundState;
import com.akirahane.momentum.core.common.context.PlayerMovementContext;
import com.akirahane.momentum.core.mixin.LivingEntityAccessor;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.common.effect.MomentumEffectType.BLOCK_FRICTION_MULTIPLIER;


public class SlideState extends GroundState {
    public static MomentumEffect TEMP_FRICTION = new MomentumEffect(
            0, 0, 0.1F, 0, -1
    );
    public static MomentumEffect TEMP_SLIDE_COOLDOWN = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );
    public static MomentumEffect TEMP_ACCELERATION = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );
    public static MomentumEffect TEMP_BLOCK_FRICTION_MULTIPLIER = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );


    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return StateType.SLIDE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).add(TEMP_FRICTION);
        SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setDuration(5);
        SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER.setMultiplier(0);
        context.getPendingEffectPool().get(BLOCK_FRICTION_MULTIPLIER).add(
                SlideState.TEMP_BLOCK_FRICTION_MULTIPLIER
        );

        if (TEMP_SLIDE_COOLDOWN.getDuration() == 0) {
            Vec3 velocity = player.getDeltaMovement();
            float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower() * 1.2F;
            LOGGER.debug("player.getJumpPower() {}", jumpPower);
            player.setDeltaMovement(
                    velocity.x * jumpPower / velocity.horizontalDistance(),
                    velocity.y,
                    velocity.z * jumpPower / velocity.horizontalDistance()
            );
            TEMP_SLIDE_COOLDOWN.setDuration(ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get());
            context.getPendingEffectPool().get(MomentumEffectType.SLIDE_COOLDOWN).add(TEMP_SLIDE_COOLDOWN);
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(TEMP_FRICTION);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(TEMP_ACCELERATION);
        context.getPendingEffectPool().get(MomentumEffectType.BLOCK_FRICTION_MULTIPLIER)
                .remove(TEMP_BLOCK_FRICTION_MULTIPLIER);
        player.setSprinting(false);
    }
}
