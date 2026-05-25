package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.Momentum.MODID;


public class SlideState extends GroundState {

    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        if (!context.isLowerCenter()) {
            return WalkState.checkChildTransition(player, context);
        }
        if (player.getDeltaMovement().horizontalDistance() * 20 <= ServerConfig.MIN_SLIDE_SPEED.get()) {
            return ProneState.checkChildTransition(player, context);
        }
        return StateType.SLIDE.getState();
    }

    public static void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);

        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x * 5, velocity.y, velocity.z * 5);
    }

    public static void onExit(Player player, PlayerMovementContext context) {
        player.setForcedPose(null);
        context.setNoMoveInput(false);
    }
}
