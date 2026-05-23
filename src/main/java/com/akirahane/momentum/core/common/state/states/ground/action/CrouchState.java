package com.akirahane.momentum.core.common.state.states.ground.action;

import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.client.init.ModKeyMappings.LOWER_CENTER_KEY_MAPPING;

public class CrouchState extends GroundState {
    public CrouchState(PlayerMovementContext data) {
        super(data);
    }

//    @Override
//    public void enter(MovementState previousState, Player player) {
//        player.setForcedPose(Pose.SWIMMING);
//    }
//
//    /**
//     * 离开状态时调用一次
//     */
//    @Override
//    public void exit(MovementState nextState, Player player) {
//        player.setForcedPose(null);
//    }

    @Override
    public void tickEffect(Player player) {
        if (LOWER_CENTER_KEY_MAPPING.get().isDown()) {
            LOGGER.debug("[Pose.SWIMMING]");
            player.setForcedPose(Pose.SWIMMING);
        } else {
            LOGGER.debug("[Pose.SWIMMING] exit");
            player.setForcedPose(null);
        }
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext data) {
        if (!LOWER_CENTER_KEY_MAPPING.get().isDown()) {
            return GroundState.newStateCheck(player, nowState, data);
        }
        if (!(nowState.getClass() == CrouchState.class)) {
            return new CrouchState(data);
        }
        return nowState;
    }
}
