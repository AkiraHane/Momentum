package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.PowerJumpState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
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
        return ServerConfig.ENABLE_WALL_HANG.getAsBoolean() && ClientConfig.ENABLE_WALL_HANG.getAsBoolean() &&
                !player.onGround() &&
                context.isHasLedge() &&
                !Vec3.ZERO.equals(context.getWallNormal()) &&
                (!LOWER_CENTER.get().isDown() || Mth.abs(context.getInputWallAngle()) < 90) &&
//                (Mth.abs(context.getLookWallAngle()) < 60 || Mth.abs(context.getInputWallAngle()) < 60) &&
                player.fallDistance <= player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE) * 2 &&
                player.getDeltaMovement().y <= 0.1 &&
                !checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_HANG);
        return Minecraft.getInstance().options.keyShift.isDown();
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 挂墙用更宽松的自保持检查（去掉 onGround/fallDistance/下落速度限制）
        return withPredicate(DEFAULT_CHAIN, StateType.WALL_HANG,
                (p, c) -> (!LOWER_CENTER.get().isDown() || Mth.abs(c.getInputWallAngle()) < 90)
                        && c.isHasLedge() && !Vec3.ZERO.equals(c.getWallNormal())
                        && !checkKey(p, c));
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        // 同步 wallNormal 给远程玩家
        if (player.level().isClientSide() && Minecraft.getInstance().player == player) {
            int wallIndex = PlayerMovementContext.encodeWallNormal(context.getWallNormal());
            context.setTransitionWallData((byte)(wallIndex >= 0 ? wallIndex : -1));
        }
        playStateAnimation(player, WALL_HANG, context, 1, 1);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null && instance.getModifier(WALL_GRAVITY_ID) == null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    WALL_GRAVITY_ID,
                    -1,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }
        // 速降补偿: 如果在更上方找到了ledge, 把玩家拉回正确位置
        if (context.getLedgeOffsetY() > 0) {
            player.setPos(player.getX(), player.getY() + context.getLedgeOffsetY(), player.getZ());
            context.setLedgeOffsetY(0);
        }
        player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
        player.fallDistance = 0;
        context.addPermanentEffect(MomentumEffectType.FRICTION, WALL_FRICTION);
        context.setNeedSoundTick(SOUND_TICK);
        context.playWallSound(player, STEP, 0.15F, 1);
        context.setTargetArmTransform(0.15F, -10F);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
        // 如果没有按后键, 给个向墙的速度防止失误掉落
        if (Minecraft.getInstance().options.keyDown.isDown() ||
                Mth.abs(context.getInputWallAngle()) > 150 && Mth.abs(context.getInputWallAngle()) < 360 &&
                        Mth.abs(context.getLookWallAngle()) > 150
        ) {
            return;
        }
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
