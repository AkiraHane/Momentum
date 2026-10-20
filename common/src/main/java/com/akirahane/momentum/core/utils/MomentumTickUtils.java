package com.akirahane.momentum.core.utils;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.client.utils.CameraUtils;
import com.akirahane.momentum.client.utils.AnimationUtils;
import com.akirahane.momentum.client.utils.MomentumInputUtils;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.registry.MomentumItems;
import com.akirahane.momentum.platform.Services;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.context.PlayerMomentumContext.*;

public class MomentumTickUtils {
    private static final Identifier BOOSTER_SPEED_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "booster_speed");
    private static final Identifier BOOSTER_JUMP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "booster_jump");
    private static final Identifier BOOSTER_STEP_ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "booster_step");

    public static void serverTick(Player player, PlayerMomentumContext context) {
        context.tickTimers();
        boolean newHasJetBooster = checkBoosterEquipped(player);
        if (newHasJetBooster != context.isHasJetBooster()) {
            context.setHasJetBooster(newHasJetBooster);
            applyBoosterAttributes(player, newHasJetBooster);
        }
        context.setCanMomentum(checkMomentum(context.isHasJetBooster()));
        if (player.fallDistance > 0) {
            context.setLastFallDistance(player.fallDistance);
        }
    }

    public static void clientTick(Player player, PlayerMomentumContext context) {
        if (context.getSlideCooldown() == 1 && !SlideState.SLIDE.equals(context.getCurrentAnimationName())){
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.3F, 1.5F);
        }
        serverTick(player, context);
        // 保存上一 tick 姿态，供渲染插值使用。
        context.setPrevBodyHeadAngleDiff(context.getBodyHeadAngleDiff());
        context.setPrevHeadXRot(player.getXRot());
        context.setDoubleClickUp(MomentumInputUtils.isDoubleClick(context, UP));
        context.setDoubleClickDown(MomentumInputUtils.isDoubleClick(context, DOWN));
        context.setDoubleClickLeft(MomentumInputUtils.isDoubleClick(context, LEFT));
        context.setDoubleClickRight(MomentumInputUtils.isDoubleClick(context, RIGHT));
        context.setDoubleClickJump(MomentumInputUtils.isDoubleClick(context, JUMP));
        context.setDoubleClickSprint(MomentumInputUtils.isDoubleClick(context, SPRINT));
        double moveSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double jumpStrength = player.getJumpBoostPower();
        context.setJumpLimitSpeed(moveSpeed * (1 + jumpStrength) * 3);
        // 越接近速度上限，跳跃加速收益越小。
        double baseJumpAccel = moveSpeed * (1 + jumpStrength) * 1.2;
        double jumpRatio = context.getSpeed().horizontalDistance() / Math.max(context.getJumpLimitSpeed(), 0.001);
        context.setJumpAcceleration(baseJumpAccel * Math.max(0.0, 1.0 - jumpRatio * jumpRatio));
        MomentumInputUtils.setWorldInputVec(player, context);
        WallDetectionUtils.detectWall(player, context);
        context.setBodyHeadAngleDiff(Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot));
        CameraUtils.tickCameraRoll(context);
        CameraUtils.computeTargetFovBonus(player, context);
        CameraUtils.tickFovBonus(context);
        CameraUtils.tickMomentumRoll(player, context);
        AnimationUtils.tickArmOffset(context);
    }

    public static void clientTickRemote(Player player, PlayerMomentumContext context) {
        context.setPrevBodyHeadAngleDiff(context.getBodyHeadAngleDiff());
        context.setPrevHeadXRot(player.getXRot());
        context.setBodyHeadAngleDiff(Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot));
        context.tickTimers();
        WallDetectionUtils.remoteDetectWall(player, context);
    }

    public static void applyBoosterAttributes(Player player, boolean apply) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (speed == null || jump == null || step == null) {
            Constants.LOG.debug("[BOOSTER] ATTRIBUTES_SKIPPED side={} player={} equipped={} reason=missing_attribute",
                    player.level().isClientSide() ? "client" : "server", player.getId(), apply);
            return;
        }

        speed.removeModifier(BOOSTER_SPEED_ID);
        jump.removeModifier(BOOSTER_JUMP_ID);
        step.removeModifier(BOOSTER_STEP_ID);

        if (apply) {
            speed.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_SPEED_ID, 0.04, AttributeModifier.Operation.ADD_VALUE));
            jump.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_JUMP_ID, 0.2, AttributeModifier.Operation.ADD_VALUE));
            step.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_STEP_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
        }
        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("[BOOSTER] ATTRIBUTES side={} player={} dimension={} equipped={} speed={} jump={} step={}",
                    player.level().isClientSide() ? "client" : "server", player.getId(), player.level().dimension().identifier(),
                    apply, speed.getValue(), jump.getValue(), step.getValue());
        }
    }

    private static boolean checkBoosterEquipped(Player player) {
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(MomentumItems.JET_BOOSTER_ITEM.get())) {
            return true;
        }
        return Services.PLATFORM.hasAccessoryJetBooster(player);
    }

    private static boolean checkMomentum(boolean hasJetBooster) {
        if (ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.get()) {
            return true;
        } else {
            return hasJetBooster;
        }
    }
}
