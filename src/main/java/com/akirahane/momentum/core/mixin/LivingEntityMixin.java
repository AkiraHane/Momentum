package com.akirahane.momentum.core.mixin;

import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.init.InitAttachments;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.WaypointTransmitter;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
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
        if (!((Object) this instanceof Player player)) {
            return original;
        }

        MovementStateMachine stateMachine = this.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine == null) return original;

        // 示例：滑铲状态下清空输入，防止玩家主动加速
        if (stateMachine.getCurrentState().getStateType() == StateType.SLIDE) {
            return Vec3.ZERO;
        }

//        // 示例：跑墙状态下重定向输入
//        if (model.getCurrentStateType() == MovementStateType.WALL_RUN) {
//            return new Vec3(0, 0, original.z); // 只保留前后输入
//        }
//
//        // 示例：喷射器空中操控力增强
//        if (!player.onGround() && model.isHasJetBooster()) {
//            return original.scale(model.getAirControlMultiplier());
//        }

        return original;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "friction")
    private float friction(float input) {
        if (!((Object) this instanceof Player player)) {
            return input;
        }

        MovementStateMachine stateMachine = this.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine == null) return input;

        // 示例：滑铲状态下清空输入，防止玩家主动加速
        if (stateMachine.getCurrentState().getStateType() == StateType.SLIDE) {
            return input + ((1F - input) / 2);
        }
        return input;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "blockFriction")
    private float blockFriction(float input) {
        if (!((Object) this instanceof Player player)) {
            return input;
        }

//        MovementStateMachine stateMachine = this.getData(InitAttachments.MOVEMENT_STATE);
//        if (stateMachine == null) return input;
//
//        // 示例：滑铲状态下清空输入，防止玩家主动加速
//        if (stateMachine.getCurrentState().getStateType() == MovementStateType.SLIDE) {
//            return input + ((1F - input) / 2);
//        }
        return input;
    }
}
