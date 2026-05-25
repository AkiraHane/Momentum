package com.akirahane.momentum.core.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
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
//
//    @Shadow
//    private Level level;
//
//    @Shadow
//    private static native List<VoxelShape> collectColliders(@Nullable Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox);

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
            @Local(name = "entityColliders") List<VoxelShape> entityColliders,
            @Local(name = "movementStep") Vec3 movementStep,
            @Local(name = "onGroundAfterCollision") boolean onGroundAfterCollision
    ) {
        // 已经上坡则不需要下坡
        if (!cir.getReturnValue().equals(movementStep)) {
            return;
        }
        if (!((Object) this instanceof LocalPlayer player)) {
            return;
        }
        Entity thisE = (Entity) (Object) this;

        entityColliders = this.level().getEntityCollisions(
                thisE,
                aabb.expandTowards(movement.subtract(0, this.maxUpStep(), 0))
        );
        // 正常行走无法落地才考虑要不要下坡
        if (this.maxUpStep() > 0.0F && !onGroundAfterCollision) {
            AABB stepDownAABB = aabb.expandTowards(movement.x, (double) -this.maxUpStep(), movement.z);
            // 似乎是原版修复浮点精度的, 但是不太理解, 目前不知道去掉会有什么bug, 推测是落地判定相关, 所以先保留
            if (!onGroundAfterCollision) {
                stepDownAABB = stepDownAABB.expandTowards((double) 0.0F, (double) -1.0E-5F, (double) 0.0F);
            }
            // 获取所有碰撞
            List<VoxelShape> colliders = collectColliders(thisE, this.level, entityColliders, stepDownAABB);
            float stepHeightToSkip = (float) movementStep.y;
            float[] candidateStepDownHeights = momentum$collectCandidateStepDownHeights(aabb, colliders, -this.maxUpStep(), stepHeightToSkip);

            for (float candidateStepDHeight : candidateStepDownHeights) {
                Vec3 stepFromGround = collideWithShapes(new Vec3(movement.x, (double) candidateStepDHeight, movement.z), aabb, colliders);
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
            double coord = (Double) collider.max(Direction.Axis.Y);
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
