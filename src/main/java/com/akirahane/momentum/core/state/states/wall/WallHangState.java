package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.WALL_FRICTION;
import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WallHangState extends BaseState {
    // 动画名称
    public static final String WALL_HANG = "wall_hang";
    public static final String WALL_HANG_RIGHT = "wall_hang_right";
    public static final String WALL_HANG_LEFT = "wall_hang_left";

    public static boolean canWallHang(Player player, PlayerMovementContext context) {
        return context.isHasLedge() &&
                !Minecraft.getInstance().options.keyJump.isDown() &&
                !Minecraft.getInstance().options.keyShift.isDown() &&
                player.fallDistance <= player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) * 3;

    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if ((context.isLowerCenter() || !context.isHasLedge() || Minecraft.getInstance().options.keyShift.isDown()) &&
                AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if ((context.isLowerCenter() || !context.isHasLedge() || Minecraft.getInstance().options.keyShift.isDown()) &&
                WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        return StateType.WALL_HANG.getState();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        playStateAnimation(player, WALL_HANG, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0;
        context.addPermanentEffect(MomentumEffectType.FRICTION, WALL_FRICTION);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        clientTickRemote(player, context);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        float yaw = player.getYRot();
        Vec3 forward = new Vec3(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw)));
        float strafe = (float) (forward.x * context.getSpeed().z - forward.z * context.getSpeed().x);
        float speed = (float) context.getSpeed().horizontalDistance() * 20;
        if (strafe > 0) {
            playStateAnimation(player, WALL_HANG_RIGHT, context, 4, speed);
        } else if (strafe < 0) {
            playStateAnimation(player, WALL_HANG_LEFT, context, 4, speed);
        } else {
            playStateAnimation(player, WALL_HANG, context, 4, 1);
        }
        // 等于0不需要处理, 直接暂停了
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        context.removeEffect(MomentumEffectType.FRICTION, WALL_FRICTION);
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_HANG;
    }
}
