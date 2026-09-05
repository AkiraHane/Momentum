package com.akirahane.momentum.platform;

import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.init.InitItems;
import com.akirahane.momentum.mixin.LivingEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class NeoForgeGameplayPlatform implements GameplayPlatform {
    @Override
    public boolean hasJetBooster(Player player) {
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(InitItems.JET_BOOSTER_ITEM.get())) {
            return true;
        }
        return CuriosCompat.isLoaded() && CuriosHandler.hasJetBooster(player);
    }

    @Override
    public float jumpPower(Player player) {
        return ((LivingEntityAccessor) player).invokeGetJumpPower();
    }

    @Override
    public MovementStateMachine movementState(Player player) {
        return player.getData(InitAttachments.MOVEMENT_STATE);
    }

    @Override
    public boolean hasMovementState(Player player) {
        return player.hasData(InitAttachments.MOVEMENT_STATE);
    }

    @Override
    public boolean isMomentumEnabled(Player player) {
        return player.getData(InitAttachments.MOMENTUM_ENABLED);
    }

    @Override
    public void setMomentumEnabled(Player player, boolean enabled) {
        player.setData(InitAttachments.MOMENTUM_ENABLED, enabled);
    }

    @Override
    public void setForcedPose(Player player, Pose pose) {
        player.setForcedPose(pose);
    }

    @Override
    public SoundType soundType(BlockState state, Level level, BlockPos pos, Player player) {
        return state.getSoundType(level, pos, player);
    }

    @Override
    public float blockFriction(BlockState state, Level level, BlockPos pos, Player player) {
        return state.getFriction(level, pos, player);
    }
}
