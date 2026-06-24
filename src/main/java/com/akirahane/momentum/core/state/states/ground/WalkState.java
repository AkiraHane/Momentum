package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class WalkState extends BaseState {
    // 动画名称 ice_slide
    public static String ICE_SLIDE = "ice_slide";
    public static boolean canWalk(Player player, PlayerMovementContext context) {
        return player.onGround();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        this.clientTickRemote(player, context);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        BlockPos posBelow = player.getBlockPosBelowThatAffectsMyMovement();
        float blockFriction = player.onGround() ? player.level().getBlockState(posBelow).getFriction(player.level(), posBelow, player) : 1.0F;
        if (blockFriction > 0.95F && context.getSpeed().horizontalDistance() > 0.1F) {
            float speed = (float) Math.min(context.getSpeed().horizontalDistance() * 1.5 , 1);
            playStateAnimation(player, ICE_SLIDE, context, 6, speed);
        } else if ((blockFriction < 0.95F || context.getSpeed().horizontalDistance() < 0.25F) && !IDLE.equals(context.getCurrentAnimationName())){
            playStateAnimation(player, IDLE, context);
        }
    }

    @Override
    public StateType getStateType() {
        return StateType.WALK;
    }
}
