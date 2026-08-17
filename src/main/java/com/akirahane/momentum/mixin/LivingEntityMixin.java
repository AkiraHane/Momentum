package com.akirahane.momentum.mixin;

import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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

import static com.akirahane.momentum.config.ServerConfig.CLIMB_BOOST_MULTIPLIER;
import static com.akirahane.momentum.core.effect.MomentumEffectType.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, WaypointTransmitter, ILivingEntityExtension {

    // 日志
    @Shadow
    public float yBodyRot;

    @Shadow
    public float yHeadRot;

    protected LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyVariable(method = "travelInWater", at = @At("STORE"), name = "slowDown")
    private float slowDown(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        original = Math.clamp(1F - (float) stateMachine.applyEffect(1F - Math.max(0, original), FRICTION), 0F, 1F);
        return original;
    }

    @ModifyVariable(method = "travelInWater", at = @At(value = "STORE", ordinal = 0), name = "waterWalker")
    private float momentum$adjustWaterWalker(float original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType() == StateType.ORIGINAL) {
            return original;
        }

        double submergedRatio = Math.min(1.0, self.getFluidHeight(FluidTags.WATER) / self.getBbHeight());
        // 没入越少 → waterWalker 越接近 1（陆地表现）
        float airRatio = (float) (1.0 - submergedRatio);

        return Math.max(original, airRatio);
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
        original = Math.clamp(1F - (float) stateMachine.applyEffect(1F - Math.max(0, original), BLOCK_FRICTION), 0F, 1F);
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
        original = Math.clamp(1F - (float) stateMachine.applyEffect(1F - Math.max(0, original), FRICTION), 0F, 1F);
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
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        // =================== 内容 ===================
        return ServerConfig.AIR_FRICTION.get().floatValue();
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
        original = stateMachine.applyEffect(
                original.subtract(stateMachine.getContext().getOldDeltaMovement()),
                player.getYRot(),
                ACCELERATION
        ).add(stateMachine.getContext().getOldDeltaMovement());
        Vec3 limitSpeed = stateMachine.applyEffect(Vec3.ZERO, player.getYRot(), LIMIT_ACCELERATION_SPEED);
        if (limitSpeed.length() != 0) {
            // 限制每 tick 的水平速度变化量：超过 limitSpeed 水平时等比例缩减（保留方向），
            // 避免旧逻辑回退到上一 tick 速度（丢方向）导致瞬间转向时速度被清零
            Vec3 old = stateMachine.getContext().getOldDeltaMovement();
            Vec3 delta = original.subtract(old);
            double dh = delta.horizontalDistance();
            double limitH = limitSpeed.horizontalDistance();
            if (dh > limitH) {
                double scale = limitH / dh;
                original = new Vec3(old.x + delta.x * scale, old.y + delta.y, old.z + delta.z * scale);
            }
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
        Vec3 current = self.getDeltaMovement();
        float angle = self.getYRot() * ((float) Math.PI / 180F);
        Vec3 acceleration = new Vec3(
                (double) (-Mth.sin((double) angle)) * stateMachine.getContext().getJumpAcceleration(),
                (double) 0.0F,
                (double) Mth.cos((double) angle) * stateMachine.getContext().getJumpAcceleration()
        );
        Vec3 boosted = current.add(acceleration);
        double currentH = current.horizontalDistance();
        double boostedH = boosted.horizontalDistance();

        if (boostedH > stateMachine.getContext().getJumpLimitSpeed() && currentH < stateMachine.getContext().getJumpLimitSpeed()) {
            Vec3 clamped = new Vec3(boosted.x * stateMachine.getContext().getJumpLimitSpeed() / boostedH, 0, boosted.z * stateMachine.getContext().getJumpLimitSpeed() / boostedH);
            Vec3 diff = clamped.subtract(current.x, 0, current.z);
            original.call(self, new Vec3(diff.x, 0, diff.z));
        } else if (currentH < stateMachine.getContext().getJumpLimitSpeed()) {
            original.call(self, acceleration);
        } else {
            // 已超速：加零，即不额外增加水平动量（原版疾跑跳 boost 被上方自定义加速取代）
            original.call(self, Vec3.ZERO);
        }
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

    // 在一些动作的时候阻止身体旋转


    @Inject(method = "tickHeadTurn", at = @At("HEAD"), cancellable = true)
    private void momentum$lockBodyRotation(float yBodyRotT, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return;
        }
        StateType state = stateMachine.getCurrentState().getStateType();

        if (StateType.WALL_CLIMB.equals(state) ||
                StateType.WALL_RUN.equals(state) ||
                StateType.WALL_SLIDE.equals(state) ||
                StateType.WALL_HANG.equals(state)
        ) {
            PlayerMovementContext context = stateMachine.getContext();
            Vec3 wallNormal = context.getWallNormal();

            if (wallNormal.lengthSqr() > 0.001) {
                // 面对墙面 = 朝着 wallNormal 的反方向看
                // wallNormal 是墙面朝外的法线，取反就是面对墙
                float targetBodyRot = (float) Math.toDegrees(Math.atan2(-wallNormal.x, wallNormal.z));

                // 平滑过渡到目标角度
                float diff = Mth.wrapDegrees(targetBodyRot - this.yBodyRot);
                this.yBodyRot += diff * 0.3f;
                if (StateType.WALL_HANG.equals(state) && context.getSpeed().horizontalDistance() * 20 > 0.05) {
                    float MAX_HEAD_DIFF = 60.0F; // 可调
                    float headDiff = Mth.wrapDegrees(this.yHeadRot - this.yBodyRot);
                    if (Math.abs(headDiff) > MAX_HEAD_DIFF) {
                        this.yHeadRot = this.yBodyRot + Math.signum(headDiff) * MAX_HEAD_DIFF;
                    }
                }
            }

            ci.cancel();
        }
    }

    // 上下梯加速
    @ModifyReturnValue(
            method = "handleOnClimbable",
            at = @At("RETURN")
    )
    private Vec3 momentum$boostClimbSpeed(Vec3 original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return original;
        if (!self.level().isClientSide()) return original;
        if (!self.onClimbable()) return original;
        if (!Minecraft.getInstance().options.keySprint.isDown()) return original;

        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }

        if (!stateMachine.getCurrentState().getStateType().equals(StateType.WALL_CLIMB) &&
                !stateMachine.getCurrentState().getStateType().equals(StateType.WALL_SLIDE)) {
            return original;
        }
        Double multiplier = CLIMB_BOOST_MULTIPLIER.get();
        return new Vec3(
                original.x,
                original.y * multiplier,
                original.z
        );
    }

}
