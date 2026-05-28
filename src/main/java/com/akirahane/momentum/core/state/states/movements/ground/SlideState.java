package com.akirahane.momentum.core.state.states.movements.ground;

import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.movements.GroundState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import com.akirahane.momentum.config.ServerConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.effect.MomentumEffectType.BLOCK_FRICTION_MULTIPLIER;


public class SlideState extends GroundState {
    public static MomentumEffect SLIDE_FRICTION = new MomentumEffect(
            0, 0, 0.1F, 0, -1
    );
    public static MomentumEffect SLIDE_COOLDOWN = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );
    public static MomentumEffect SLIDE_ACCELERATION = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );
    public static MomentumEffect SLIDE_BLOCK_FRICTION = new MomentumEffect(
            0, 0, 1.0F, 0, 0
    );


    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return StateType.SLIDE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).add(SLIDE_FRICTION);
        SlideState.SLIDE_BLOCK_FRICTION.setDuration(4);
        SlideState.SLIDE_BLOCK_FRICTION.setMultiplier(0);
        context.getPendingEffectPool().get(BLOCK_FRICTION_MULTIPLIER).add(
                SlideState.SLIDE_BLOCK_FRICTION
        );

        if (SLIDE_COOLDOWN.getDuration() == 0) {
            Vec3 velocity = player.getDeltaMovement();
            float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower() * 1.2F;
            LOGGER.debug("player.getJumpPower() {}", jumpPower);
            player.addDeltaMovement(
                    new Vec3(
                            velocity.x * jumpPower / velocity.horizontalDistance(),
                            0,
                            velocity.z * jumpPower / velocity.horizontalDistance()
                    )
            );
            SLIDE_COOLDOWN.setDuration(ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get());
            context.getPendingEffectPool().get(MomentumEffectType.SLIDE_COOLDOWN).add(SLIDE_COOLDOWN);
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(SLIDE_FRICTION);
        context.getPendingEffectPool().get(MomentumEffectType.FRICTION).remove(SLIDE_ACCELERATION);
        context.getPendingEffectPool().get(MomentumEffectType.BLOCK_FRICTION_MULTIPLIER)
                .remove(SLIDE_BLOCK_FRICTION);
        player.setSprinting(false);
        context.setSlopeUnitVector(Vec3.ZERO);
    }

    @Override
    public void clientTick(LocalPlayer player, PlayerMovementContext context) {
        super.clientTick(player, context);
        if (context.getBlockStep() != 0 && !player.onGround()) {
            Vec3 velocity = context.getSpeed();
            player.setDeltaMovement(
                    new Vec3(
                            velocity.x,
                            velocity.y,
                            velocity.z
                    )
            );
            context.setBlockStep(0);
        }
    }
}
