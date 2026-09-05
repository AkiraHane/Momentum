package com.akirahane.momentum.fabric.platform;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.fabric.init.FabricAttachments;
import com.akirahane.momentum.fabric.init.FabricItems;
import com.akirahane.momentum.fabric.compat.trinkets.FabricTrinketsCompat;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import com.akirahane.momentum.platform.GameplayPlatform;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricGameplayPlatform implements GameplayPlatform {
    @Override
    public boolean hasJetBooster(Player player) {
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(FabricItems.JET_BOOSTER)) {
            return true;
        }
        return FabricTrinketsCompat.hasJetBooster(player);
    }

    @Override
    public float jumpPower(Player player) {
        return ((LivingEntityAccessor) player).invokeGetJumpPower();
    }

    @Override
    public MovementStateMachine movementState(Player player) {
        return attachments(player).getAttachedOrCreate(
                FabricAttachments.MOVEMENT_STATE, () -> new MovementStateMachine(player));
    }

    @Override
    public boolean hasMovementState(Player player) {
        return attachments(player).hasAttached(FabricAttachments.MOVEMENT_STATE);
    }

    @Override
    public boolean isMomentumEnabled(Player player) {
        return attachments(player).getAttachedOrCreate(FabricAttachments.MOMENTUM_ENABLED);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        attachments(player).setAttached(FabricAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void setForcedPose(Player player, Pose pose) {
        ((PlayerForcedPoseAccess) player).momentum$setForcedPose(pose);
    }

    @Override
    public SoundType soundType(BlockState state, Level level, BlockPos pos, Player player) {
        return state.getSoundType();
    }

    @Override
    public float blockFriction(BlockState state, Level level, BlockPos pos, Player player) {
        return state.getBlock().getFriction();
    }

    private static AttachmentTarget attachments(Player player) {
        return (AttachmentTarget) player;
    }
}
