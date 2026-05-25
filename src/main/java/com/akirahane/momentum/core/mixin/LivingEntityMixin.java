package com.akirahane.momentum.core.mixin;

import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.init.InitAttachments;
import com.akirahane.momentum.server.config.ServerConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.WaypointTransmitter;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, WaypointTransmitter, ILivingEntityExtension {


    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyVariable(
            method = "travelInAir",
            at = @At("HEAD"),       // 方法最开头，在所有代码之前
            argsOnly = true,        // 只匹配方法参数，不匹配局部变量
            // 第一个Vec3参数
            name = "input")
    private Vec3 modifyTravelInput(Vec3 original) {
        if (!((Object) this instanceof LocalPlayer player)) {
            return original;
        }

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        PlayerMovementContext.TempData tempData;
        if (stateMachine == null) return original;

        if (stateMachine.getContext().isNoMoveInput()) {
            return Vec3.ZERO;
        }
        tempData = stateMachine.getContext().tempMap.get(
                PlayerMovementContext.TempDataType.TEMP_ACCELERATION_LIMIT_SPEED
        );
        if (tempData.getDuration() != 0 && tempData.getValue() <= player.getDeltaMovement().horizontalDistance()) {
            return Vec3.ZERO;
        }
        tempData = stateMachine.getContext().tempMap.get(
                PlayerMovementContext.TempDataType.TEMP_ACCELERATION
        );
        if (tempData.getDuration() != 0) {
            original = original.multiply(tempData.getMultiplier(), 1, tempData.getMultiplier());
        }

        return original;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "friction")
    private float friction(float input) {
        if (!((Object) this instanceof LocalPlayer player)) {
            return input;
        }

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine == null) return input;

        // 示例：滑铲状态下清空输入，防止玩家主动加速
        if (stateMachine.getContext().getTempMap()
                .get(PlayerMovementContext.TempDataType.TEMP_FRICTION).getDuration() != 0) {
            input = 1F - (1F - input) * stateMachine.getContext().getTempMap()
                    .get(PlayerMovementContext.TempDataType.TEMP_FRICTION).getMultiplier();
        }
        return input;
    }

    @ModifyConstant(
            method = "travelInAir",
            constant = @Constant(floatValue = 0.91F)
    )
    private float modifyAirFriction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        if (!player.getData(InitAttachments.MOVEMENT_STATE).getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return ServerConfig.AIR_FRICTION.get().floatValue();  // 非原版模式下减少空气阻力
        }
        return original;
    }
}
