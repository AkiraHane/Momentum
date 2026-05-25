package com.akirahane.momentum.core.mixin;

import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.init.InitAttachments;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    private Level level;

    @Shadow
    private static List<VoxelShape> collectColliders(@Nullable Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static float[] collectCandidateStepUpHeights(AABB boundingBox, List<VoxelShape> colliders, float maxStepHeight, float stepHeightToSkip) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public abstract float maxUpStep();

    @Shadow
    public abstract boolean onGround();

    @Shadow
    private static Vec3 collideWithShapes(Vec3 movement, AABB boundingBox, List<VoxelShape> shapes) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public abstract Level level();

    protected EntityMixin(EntityType<?> type, Level level) {
    }

    // 自动下坡
    @Inject(
            method = "collide",
            at = @At("RETURN"),
            cancellable = true
    )
    private void addStepDown(
            Vec3 movement,
            CallbackInfoReturnable<Vec3> cir,
            @Local(name = "aabb") AABB aabb,
            @Local(name = "movementStep") Vec3 movementStep,
            @Local(name = "onGroundAfterCollision") boolean onGroundAfterCollision
    ) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LocalPlayer player)) {
            return;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return;
        }
        // =================== 内容 ===================

        // 已经上坡则不需要下坡
        if (!cir.getReturnValue().equals(movementStep)) {
            return;
        }
        // 上升时不需要下坡
        if (movement.y >= 0) {
            return;
        }
        // 下落速度超过重力则不下坡
        if (movement.y < -player.getAttributeValue(Attributes.GRAVITY)) {
            return;
        }

        List<VoxelShape> entityColliders = this.level.getEntityCollisions(
                self,
                aabb.expandTowards(movement.subtract(0, this.maxUpStep(), 0))
        );
        // 正常行走无法落地才考虑要不要下坡
        if (this.maxUpStep() > 0.0F && (!onGroundAfterCollision)) {
            AABB stepDownAABB = aabb.expandTowards(movement.x, (double) -this.maxUpStep(), movement.z);
            // 似乎是原版修复浮点精度的, 但是不太理解, 目前不知道去掉会有什么bug, 推测是落地判定相关, 所以先保留
            if (!onGroundAfterCollision) {
                stepDownAABB = stepDownAABB.expandTowards(0.0F, -1.0E-5F, 0.0F);
            }
            // 获取所有碰撞
            List<VoxelShape> colliders = collectColliders(self, this.level, entityColliders, stepDownAABB);
            float stepHeightToSkip = (float) movementStep.y;
            float[] candidateStepDownHeights = momentum$collectCandidateStepDownHeights(aabb, colliders, -this.maxUpStep(), stepHeightToSkip);

            for (float candidateStepDHeight : candidateStepDownHeights) {
                Vec3 stepFromGround = collideWithShapes(new Vec3(movement.x, candidateStepDHeight, movement.z), aabb, colliders);
                if (stepFromGround.horizontalDistanceSqr() > 0) {
                    cir.setReturnValue(stepFromGround);
                    return;
                }
            }
        }
    }

    @Unique
    private static float[] momentum$collectCandidateStepDownHeights(AABB boundingBox, List<VoxelShape> colliders, float maxStepHeight, float stepHeightToSkip) {
        FloatSet candidates = new FloatArraySet(4);

        for (VoxelShape collider : colliders) {
            double coord = collider.max(Direction.Axis.Y);
            float relativeCoord = (float) (coord - boundingBox.minY);
            if (!(relativeCoord > 0.0F) && relativeCoord != stepHeightToSkip) {
                if (relativeCoord < maxStepHeight) {
                    continue;
                }

                candidates.add(relativeCoord);
            }
        }

        float[] sortedCandidates = candidates.toFloatArray();
        FloatArrays.unstableSort(sortedCandidates);
        return sortedCandidates;
    }
}
