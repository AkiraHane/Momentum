package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.STEP;
import static com.akirahane.momentum.core.context.PlayerMovementContext.WALL_FRICTION;

public class WallHangState extends BaseState {

    private static final int SOUND_TICK = 10;
    // 动画名称
    public static final String WALL_HANG = "wall_hang";
    public static final String WALL_HANG_LOOK_RIGHT = "wall_hang_look_right";
    public static final String WALL_HANG_LOOK_LEFT = "wall_hang_look_left";
    public static final String WALL_HANG_RIGHT = "wall_hang_right";
    public static final String WALL_HANG_LEFT = "wall_hang_left";

    public static boolean canWallHang(Player player, PlayerMovementContext context) {
        return context.isHasLedge() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                !Vec3.ZERO.equals(context.getInputVec()) && Mth.abs(context.getInputWallAngle()) < 90 &&
                player.fallDistance <= player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) * 2 &&
                !checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_HANG);
        return Minecraft.getInstance().options.keyShift.isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        playStateAnimation(player, WALL_HANG, context, 1, 1);
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
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        // 如果没有按后键, 给个向墙的速度防止失误掉落
        if (!Minecraft.getInstance().options.keyDown.isDown()) {
            Vec3 wallNormal = context.getWallNormal();
            Vec3 currentMovement = player.getDeltaMovement();

            // 给一个轻微的朝墙速度，防止玩家因为微小偏移脱离墙面
            double pushStrength = 0.05;
            player.setDeltaMovement(
                    currentMovement.x + wallNormal.x * pushStrength,
                    currentMovement.y,
                    currentMovement.z + wallNormal.z * pushStrength
            );
        }
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        float speed = (float) context.getSpeed().horizontalDistance() * 20;
        if (speed > 0.05) {
            if (context.getInputWallAngle() < 0) {
                playStateAnimation(player, WALL_HANG_RIGHT, context, 2, speed);
            } else {
                playStateAnimation(player, WALL_HANG_LEFT, context, 2, speed);
            }
            context.setNeedSoundTick(context.getNeedSoundTick() - speed);
            if (context.getNeedSoundTick() <= 0) {
                context.playWallSound(player, STEP, 0.15F, 1);
                context.setNeedSoundTick(context.getNeedSoundTick() + SOUND_TICK);
            }
        } else {
            context.setNeedSoundTick(SOUND_TICK);
            if (Mth.abs(context.getLookWallAngle()) < 45) {
                playStateAnimation(player, WALL_HANG, context, 2, 1);
            } else if (context.getLookWallAngle() < -70) {
                if (WALL_HANG_LOOK_LEFT.equals(context.getCurrentAnimationName())) {
                    playStateAnimation(player, WALL_HANG, context, 4, 1);
                } else {
                    playStateAnimation(player, WALL_HANG_LOOK_RIGHT, context, 4, 1);
                }
            } else if (context.getLookWallAngle() > 70) {
                if (WALL_HANG_LOOK_RIGHT.equals(context.getCurrentAnimationName())) {
                    playStateAnimation(player, WALL_HANG, context, 4, 1);
                } else {
                    playStateAnimation(player, WALL_HANG_LOOK_LEFT, context, 4, 1);
                }
            }
        }
        // 等于0不需要处理, 直接暂停了
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
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
