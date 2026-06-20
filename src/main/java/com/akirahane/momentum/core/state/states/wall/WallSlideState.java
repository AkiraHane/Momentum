package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;
import static com.akirahane.momentum.core.state.states.air.AirborneState.canAirborne;
import static com.akirahane.momentum.core.state.states.ground.WalkState.canWalk;
import static com.akirahane.momentum.core.state.states.wall.WallClimbState.canWallClimb;
import static com.akirahane.momentum.core.state.states.wall.WallHangState.canWallHang;
import static com.akirahane.momentum.core.state.states.wall.WallRunState.canWallRun;
import static net.minecraft.world.level.block.SoundType.*;

public class WallSlideState extends BaseState {
    // 动画名称
    public static String WALL_SLIDE = "wall_slide";

    public static boolean canWallSlide(Player player, PlayerMovementContext context) {
        return !player.onGround() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                context.isHasFaceWall() &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 60 &&
                Mth.abs(context.getLookWallAngle()) < 60;
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (WallKickState.canWallKick(player, context)) {
            return StateType.WALL_KICK.getState();
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
        context.addPermanentEffect(MomentumEffectType.ACCELERATION, AIR_ACCELERATION);
        context.addPermanentEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
        playStateAnimation(player, WALL_SLIDE, context, 4, 1);
    }

    @Override
    public void serverTick(Player player, PlayerMovementContext context) {
        super.serverTick(player, context);
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        // 按照重力倍率衰减掉落伤害
        player.fallDistance *= 0.9;
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (context.getSpeed().y > 0 && instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        } else if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -0.9,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        if (player.tickCount % 2 == 0) {
            player.playSound(
                    GRASS.getStepSound(),
                    0.05F,
                    1F
            );
        }
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        context.removeEffect(MomentumEffectType.ACCELERATION, AIR_ACCELERATION);
        context.removeEffect(MomentumEffectType.LIMIT_ACCELERATION_SPEED, AIR_LIMIT_ACCELERATION);
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
