package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.MomentumUtils;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.WaypointTransmitter;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.akirahane.momentum.core.effect.MomentumEffectType.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, WaypointTransmitter, ILivingEntityExtension {

    // 日志
    @Shadow
    protected abstract double getEffectiveGravity();

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
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        if (stateMachine.getContext().isNoMoveInput()) {
            return Vec3.ZERO;
        }
        return original;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "blockFriction")
    private float blockFriction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        original = Math.clamp(0F, 1F - (float) stateMachine.applyEffect(1F - Math.max(0, original), BLOCK_FRICTION), 1F);
        return original;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "friction")
    private float friction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        original = Math.clamp(0F, 1F - (float) stateMachine.applyEffect(1F - Math.max(0, original), FRICTION), 1F);
        return original;
    }

    @ModifyConstant(
            method = "travelInAir",
            constant = @Constant(floatValue = 0.91F)
    )
    public float modifyAirFriction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        return MomentumUtils.getAirFriction(player);
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "movement")
    private Vec3 movement(Vec3 original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        original = stateMachine.applyEffect(
                original.subtract(stateMachine.getContext().getDeltaMovement()),
                player.getYRot(),
                ACCELERATION
        ).add(stateMachine.getContext().getDeltaMovement());
        Vec3 limitSpeed = stateMachine.applyEffect(Vec3.ZERO, player.getYRot(), LIMIT_ACCELERATION_SPEED);
        if (limitSpeed.length() != 0) {
            original = new Vec3(
                    // 这里存了上一tick的速度
                    Math.abs(original.x) > Math.abs(limitSpeed.x)
                            ? (
                            Math.abs(original.x) < Math.abs(stateMachine.getContext().getDeltaMovement().x)
                                    ? original.x
                                    : stateMachine.getContext().getDeltaMovement().x)
                            : original.x,
                    Math.abs(original.y) > Math.abs(limitSpeed.y)
                            ? (
                            Math.abs(original.y) < Math.abs(stateMachine.getContext().getDeltaMovement().y)
                                    ? original.y
                                    : stateMachine.getContext().getDeltaMovement().y)
                            : original.y,
                    Math.abs(original.z) > Math.abs(limitSpeed.z)
                            ? (
                            Math.abs(original.z) < Math.abs(stateMachine.getContext().getDeltaMovement().z)
                                    ? original.z
                                    : stateMachine.getContext().getDeltaMovement().z)
                            : original.z
            );
        }

        return original;
    }

//
//    // https://github.com/LlamaLad7/MixinExtras/wiki/WrapOperation
    @WrapOperation(method = "jumpFromGround",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void wrapSprintBoost(LivingEntity self, Vec3 originalBoost, Operation<Void> original) {
        if (!(self instanceof Player player)) {
            original.call(self, originalBoost);
            return;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            original.call(self, originalBoost);
            return;
        }
        double moveSpeed = self.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double jumpStrength = self.getAttributeValue(Attributes.JUMP_STRENGTH);
        // TODO 完善公式, 添加连跳惩罚
        double jumpLimitSpeed = moveSpeed * (1 + jumpStrength) * 2;
        Vec3 current = self.getDeltaMovement();
        Vec3 boosted = current.add(originalBoost);
        double currentH = current.horizontalDistance();
        double boostedH = boosted.horizontalDistance();

        if (boostedH > jumpLimitSpeed && currentH < jumpLimitSpeed) {
            Vec3 clamped = new Vec3(boosted.x * jumpLimitSpeed / boostedH, 0, boosted.z * jumpLimitSpeed / boostedH);
            Vec3 diff = clamped.subtract(current.x, 0, current.z);
            original.call(self, new Vec3(diff.x, 0, diff.z));
        } else if (currentH < jumpLimitSpeed) {
            original.call(self, originalBoost);
        }
        // 已超速则不调用
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void onJumpFromGround(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return;
        }
        if (stateMachine.getContext().isNoJump()) {
            ci.cancel();
        }
    }

}
