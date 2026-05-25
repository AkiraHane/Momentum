package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.core.mixin.LivingEntityAccessor;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;


public class SlideState extends GroundState {

    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return StateType.SLIDE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_FRICTION).setDuration(-1);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_FRICTION).setMultiplier(0.1F);

        if (context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_SLIDE_COOLDOWN).getDuration() == 0) {
            Vec3 velocity = player.getDeltaMovement();
            float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower() * 4;
            LOGGER.debug("player.getJumpPower() * 4 {}", jumpPower);
            player.setDeltaMovement(velocity.x * jumpPower, velocity.y, velocity.z * jumpPower);
            context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_SLIDE_COOLDOWN).setDuration(
                    ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get()
            );
        }
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.getTempMap().get(PlayerMovementContext.TempDataType.TEMP_FRICTION).init();
    }
}
