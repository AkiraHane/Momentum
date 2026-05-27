package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.MomentumUtils;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.akirahane.momentum.core.effect.MomentumEffectType.BLOCK_FRICTION_MULTIPLIER;
import static com.akirahane.momentum.core.state.states.movements.GroundState.JUMP_ACCELERATION_LIMIT_SPEED;

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
        if (!(self instanceof Player player)) {
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
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        original = Math.min(1F, 1F - (1F - Math.max(0, original)) * stateMachine.getContext().getEffectMap()
                .get(BLOCK_FRICTION_MULTIPLIER).getMultiplier());
        return original;
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "friction")
    private float friction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        MomentumEffect momentumEffect;
        momentumEffect = stateMachine.getContext().getEffectMap().get(
                MomentumEffectType.FRICTION
        );
        original = 1F - (1F - original) * momentumEffect.getMultiplier();
        return original;
    }

    @ModifyConstant(
            method = "travelInAir",
            constant = @Constant(floatValue = 0.91F)
    )
    public float modifyAirFriction(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        return MomentumUtils.getAirFriction(player);
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "movement")
    private Vec3 movement(Vec3 original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        MomentumEffect momentumEffect;
        momentumEffect = stateMachine.getContext().getEffectMap().get(
                MomentumEffectType.ACCELERATION
        );
        // 向量数值需要增加 tempData.getValue() 分解到垂直分向量
        if (original.horizontalDistance() > 0 && momentumEffect.getValue() != 0) {
            Vec3 slopeDir = stateMachine.getContext().getSlopeUnitVector();

            if (slopeDir.lengthSqr() > 0) {
                // 只沿坡面方向加速
                double accel = momentumEffect.getValue();
                original = original.add(
                        slopeDir.x * accel,
                        0,
                        slopeDir.z * accel
                );
            } else {
                // 没有坡面信息，保持原逻辑
                original = original.add(
                        (original.x * momentumEffect.getValue()) / original.length(),
                        (original.y * momentumEffect.getValue()) / original.length(),
                        (original.z * momentumEffect.getValue()) / original.length()
                );
            }
        }
        momentumEffect = stateMachine.getContext().getEffectMap().get(
                MomentumEffectType.ACCELERATION_LIMIT_SPEED
        );
        if (momentumEffect.getValue() != 0 && momentumEffect.getValue() <= player.getDeltaMovement().horizontalDistance()) {
            double limitX = player.getDeltaMovement().x * momentumEffect.getValue() / player.getDeltaMovement().horizontalDistance();
            double limitZ = player.getDeltaMovement().z * momentumEffect.getValue() / player.getDeltaMovement().horizontalDistance();
            original = new Vec3(
                    // 这里存了上一tick的速度
                    Math.abs(original.x) > limitX ? stateMachine.getContext().getSpeed().x : original.x,
                    original.y,
                    Math.abs(original.z) > limitZ ? stateMachine.getContext().getSpeed().z : original.z
            );
        }
        original = original.multiply(momentumEffect.getMultiplier(), 1, momentumEffect.getMultiplier());

        return original;
    }

    @Inject(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEffectiveGravity()D"))
    private void beforeGravity(Vec3 input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        double gravity = this.getEffectiveGravity();
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        stateMachine.getContext().setMixinGravity(gravity);
    }

    @ModifyVariable(method = "travelInAir", at = @At("STORE"), name = "verticalFriction")
    private float verticalFriction(float verticalFriction) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return verticalFriction;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return verticalFriction;
        }
        stateMachine.getContext().setMixinVerticalFriction(verticalFriction);
        return verticalFriction;
    }

    @ModifyConstant(method = "jumpFromGround", constant = @Constant(doubleValue = 0.2))
    private double modifySprintJumpBoost(double original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        if (JUMP_ACCELERATION_LIMIT_SPEED.getValue() != 0 &&
                JUMP_ACCELERATION_LIMIT_SPEED.getValue() <= player.getDeltaMovement().horizontalDistance()
        ) {
            return 0;
        }
        return original;
    }
}
