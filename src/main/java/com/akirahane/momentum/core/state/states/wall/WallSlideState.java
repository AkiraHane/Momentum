package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.AirborneState.canAirborne;
import static com.akirahane.momentum.core.state.states.ground.WalkState.canWalk;
import static com.akirahane.momentum.core.state.states.wall.WallClimbState.canWallClimb;
import static com.akirahane.momentum.core.state.states.wall.WallHangState.canWallHang;
import static com.akirahane.momentum.core.state.states.wall.WallRunState.canWallRun;

public class WallSlideState extends BaseState {
    // 动画名称
    public static String WALL_SLIDE = "wall_slide";

    public static boolean canWallSlide(Player player, PlayerMovementContext context) {
        return context.getWallDirection() != null &&
                context.getInputWallAngle() < 90 &&
                context.getLookWallAngle() < 45 &&
                context.getInputWallAngle() >= 0 &&
                context.getSpeed().y < 0;
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_SLIDE.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -0.9,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
        // 播放动画
        playStateAnimation(player, WALL_SLIDE, context);
    }

    @Override
    public void serverTick(Player player, PlayerMovementContext context) {
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_SLIDE;
    }
}
