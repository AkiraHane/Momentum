package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import com.akirahane.momentum.config.ServerConfig;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.core.MomentumUtils.UPHILL_DECEL_FACTOR;
import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.LOCAL_VALUE;
import static com.akirahane.momentum.core.effect.MomentumEffectType.ACCELERATION;
import static net.minecraft.world.level.block.SoundType.GRASS;


public class SlideState extends BaseState {
    // 动画名称
    public static String SLIDE = "slide";

    // 跳跃减速窗口时间
    protected int JUMP_DECELERATION_WINDOW = 5;

    public static boolean canSlide(Player player, PlayerMovementContext context) {
        return ServerConfig.ENABLE_SLIDE.getAsBoolean() && ClientConfig.ENABLE_SLIDE.getAsBoolean() &&
                player.onGround() &&
                canSlideSpeedCheck(player, context) &&
                checkKey(player, context);
    }

    public static boolean canSlideSpeedCheck(Player player, PlayerMovementContext context) {
        return context.getSpeed().horizontalDistance() * 20 >
                (player.isSprinting() ? ServerConfig.MIN_SLIDE_SPEED.get() : ServerConfig.MIN_SLIDE_SPEED.get() * 2) &&
                (context.getPendingEffectPool().get(MomentumEffectType.ACCELERATION).contains(context.SLIDE_ACCELERATION) ||
                        context.getOldSpeed().horizontalDistance() >= -context.getOldSpeed().y);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.SLIDE);
        return LOWER_CENTER.get().isDown();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        player.setForcedPose(Pose.SWIMMING);
        context.setNoMoveInput(true);
        context.addPermanentEffect(MomentumEffectType.FRICTION, context.SLIDE_FRICTION);
        context.addEffect(MomentumEffectType.BLOCK_FRICTION, context.SLIDE_BLOCK_FRICTION, JUMP_DECELERATION_WINDOW);

        Vec3 velocity = player.getDeltaMovement();
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        LOGGER.trace("player.getJumpPower() {}", jumpPower);
        int slideAccelerationCooldown = ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get();
        if (context.getSlideCooldown() != 0) {
            jumpPower /= 2;
            jumpPower *= ((float) (slideAccelerationCooldown - context.getSlideCooldown()) / slideAccelerationCooldown);
        }
        if (context.getSpeed().horizontalDistance() >= context.getJumpLimitSpeed()){
            jumpPower = 0;
        }

        player.addDeltaMovement(
                new Vec3(
                        velocity.x * jumpPower / velocity.horizontalDistance(),
                        0,
                        velocity.z * jumpPower / velocity.horizontalDistance()
                )
        );
        playStateAnimation(player, SLIDE, context, 6, 1.0f);
        context.setMomentumRollIntensity(15F);
        context.setTargetArmTransform(0.15F, -15F);
        context.setSlideCooldown(slideAccelerationCooldown);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        super.clientTick(player, context);
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

    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        Vec3 slopeDir = context.getSlopeUnitVector();
        if (Vec3.ZERO.equals(slopeDir) && context.getBlockStep() > 0){
            double riseHeight = context.getBlockStep();
            float deceleration = (float) (UPHILL_DECEL_FACTOR * Math.min(riseHeight * riseHeight, 1.0));
            context.SLIDE_ACCELERATION.setValue(new Vec3(
                    slopeDir.x * deceleration,
                    context.getBlockStep() * player.getDeltaMovement().horizontalDistance() * 0.5,
                    slopeDir.z * deceleration)
            );
            context.addEffect(ACCELERATION, context.SLIDE_ACCELERATION, 1);
            context.setBlockStep(0);
        }
        if (JUMP_DECELERATION_WINDOW >= (ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get() - context.getSlideCooldown())) {
            // 如果跳跃的时间小于冷却, 则增加移动方向的阻力
            context.addEffect(
                    MomentumEffectType.FRICTION,
                    new MomentumEffect(
                            new Vec3(0.2, 0, 0),
                            Vec3.ZERO,
                            LOCAL_VALUE,
                            5
                    ),
                    5
            );
        }
        player.setForcedPose(null);
        context.setNoMoveInput(false);
        context.removeEffect(MomentumEffectType.FRICTION, context.SLIDE_FRICTION);
        context.removeEffect(MomentumEffectType.BLOCK_FRICTION, context.SLIDE_BLOCK_FRICTION);
        player.setSprinting(false);
        context.setSlopeUnitVector(Vec3.ZERO);
        context.setSlideCooldown(ServerConfig.SLIDE_ACCELERATION_COOLDOWN.get());
        context.setMomentumRollIntensity(0F);  // 退出时关闭
    }

    @Override
    public StateType getStateType() {
        return StateType.SLIDE;
    }
}
