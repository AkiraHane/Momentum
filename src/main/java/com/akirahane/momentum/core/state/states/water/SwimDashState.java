package com.akirahane.momentum.core.state.states.water;

import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.wall.*;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.client.init.InitSounds.JET2;
import static com.akirahane.momentum.config.ServerConfig.DODGE_COOLDOWN;
import static com.akirahane.momentum.config.ServerConfig.DODGE_STORAGE;
import static com.akirahane.momentum.core.context.PlayerMovementContext.SPRINT;

public class SwimDashState extends BaseState {

    // 海豚跳动画
    public static String DOLPHIN_JUMP = "dolphin_jump";

    public static boolean canSwimDash(Player player, PlayerMovementContext context) {
        if (!ServerConfig.ENABLE_WATER_PUSH.getAsBoolean() || !ClientConfig.ENABLE_WATER_PUSH.getAsBoolean()) {
            return false;
        }
        if (!player.isUnderWater()) {
            return false;
        }
        if (DODGE_COOLDOWN.get() * DODGE_STORAGE.get() - context.getDodgeCooldown() <= DODGE_COOLDOWN.get()) {
            return false;
        }
        if (player.isSwimming()) {
            HintManager.add(WallHangHints.PUSH);
            return context.getInputBuffer()[context.getInputBufferIndex()].contains(SPRINT);
        } else {
            HintManager.add(WallHangHints.PUSH_UP);
            return Minecraft.getInstance().options.keyUp.isDown() &&
                    context.getInputBuffer()[context.getInputBufferIndex()].contains(SPRINT);
        }
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 海豚跳推进持续期自保持
        java.util.List<Transition> chain = withPredicate(DEFAULT_CHAIN, StateType.SWIM_DASH,
                (p, c) -> c.getSwimPushTimer() > 0 && (p.isUnderWater() || !p.onGround()));

        // 碰到梯子时，攀爬/滑墙必须先于海豚跳的空中自保持。只让原版可攀爬方块抢占，
        // 避免普通墙面意外打断海豚跳。先移动 WALL_SLIDE，再移动 WALL_CLIMB，保证最终顺序为
        // DODGE -> WALL_CLIMB -> WALL_SLIDE -> SWIM_DASH。
        chain = moveAfter(chain, StateType.WALL_SLIDE, StateType.DODGE,
                WallSlideState::canWallSlide);
        return moveAfter(chain, StateType.WALL_CLIMB, StateType.DODGE,
                WallClimbState::canWallClimb);
    }

    public void onEnter(Player player, PlayerMovementContext context) {
        player.setSwimming(true);
        player.setSprinting(true);
        float yRot = player.getYRot();
        float xRot = player.getXRot();
        Vec3 direction = Vec3.directionFromRotation(xRot, yRot);
        double currentSpeed = player.getDeltaMovement().horizontalDistance();
        double targetSpeed = direction.horizontalDistance() * 0.8;
        if (currentSpeed > targetSpeed) {
            // 根据转向角度衰减速度
            Vec3 currentHorizontal = player.getDeltaMovement().multiply(1, 0, 1).normalize();
            Vec3 targetHorizontal = new Vec3(direction.x, 0, direction.z).normalize();
            double dot = currentHorizontal.dot(targetHorizontal); // 1=同向, 0=90度, -1=反向
            double factor = Math.max(0, dot); // 90度以上直接归零

            double finalSpeed = currentSpeed * factor;

            if (finalSpeed < targetSpeed) {
                finalSpeed = targetSpeed;
            }

            Vec3 normalized = new Vec3(
                    direction.x,
                    !player.isSwimming() ? 0.3F : direction.y,
                    direction.z
            ).normalize();
            player.setDeltaMovement(normalized.x * finalSpeed, normalized.y, normalized.z * finalSpeed);
        } else {
            player.setDeltaMovement(
                    direction.x * 0.8,
                    !player.isSwimming() ? 0.3F : direction.y,
                    direction.z * 0.8);
        }
        if (context.isHasJetBooster()) {
            player.playSound(JET2.value(), 1F, 1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F);
        }
        context.setSwimPushTimer(10);
        // 水中推进和闪避共用冷却
        context.setDodgeCooldown(context.getDodgeCooldown() + DODGE_COOLDOWN.get());
        context.setNoJump(true);
        context.setNoMoveInput(true);
        player.playSound(
                SoundEvents.PLAYER_SWIM,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F
        );
        context.setMomentumRollIntensity(15F);
        player.setForcedPose(Pose.SWIMMING);
    }

    @Override
    public void clientTick(Player player, PlayerMovementContext context) {
        if (!player.isInWater()) {
            context.setSwimPushTimer(10);
            var instance = player.getAttribute(Attributes.GRAVITY);
            if (instance != null && instance.getModifier(DOLPHIN_GRAVITY_ID) == null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        DOLPHIN_GRAVITY_ID,
                        -0.4,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
        super.clientTick(player, context);
    }

    @Override
    public void clientTickRemote(Player player, PlayerMovementContext context) {
        if (!player.isInWater()) {
            playStateAnimation(player, DOLPHIN_JUMP, context);
        } else if (!IDLE.equals(context.getCurrentAnimationName())) {
            playStateAnimation(player, IDLE, context);
        }
    }

    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.setNoJump(false);
        context.setNoMoveInput(false);
        player.setForcedPose(null);
        player.setSwimming(false);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(DOLPHIN_GRAVITY_ID);
        }
//        player.setSprinting(false);
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM_DASH;
    }
}
