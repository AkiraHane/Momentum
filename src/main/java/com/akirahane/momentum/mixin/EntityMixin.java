package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
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

import static com.akirahane.momentum.core.MomentumUtils.setSlideAcceleration;

@Mixin(Entity.class)
public abstract class EntityMixin{

    @Shadow
    private Level level;

    @Shadow
    private static List<VoxelShape> collectColliders(@Nullable Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox) {
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
        if (!(self instanceof Player player)) {
            return;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return;
        }
        // =================== 内容 ===================

        // 已经上坡则不需要下坡
        if (!cir.getReturnValue().equals(movementStep)) {
            setSlideAcceleration(movement, cir.getReturnValue().y, stateMachine);
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

        float maxDownStep = (float) (this.maxUpStep() * Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))));

        List<VoxelShape> entityColliders = this.level.getEntityCollisions(
                self,
                aabb.expandTowards(movement.subtract(0, maxDownStep, 0))
        );
        // 正常行走无法落地才考虑要不要下坡
        if (maxDownStep > 0.0F && (!onGroundAfterCollision) && this.onGround()) {
            AABB stepDownAABB = aabb.expandTowards(
                    movement.x, -maxDownStep, movement.z
            );
            // 似乎是原版修复浮点精度的, 但是不太理解, 目前不知道去掉会有什么bug, 推测是落地判定相关, 所以先保留
            // 为了能碰撞地面, 所以特地往下拉了一个玩家的身位
            if (!onGroundAfterCollision) {
                stepDownAABB = stepDownAABB.expandTowards(
                        0.0F, -player.getBoundingBox().getYsize() - 1.0E-5F, 0.0F
                );
            }
            // 获取所有碰撞
            List<VoxelShape> colliders = collectColliders(self, this.level, entityColliders, stepDownAABB);
            float stepHeightToSkip = (float) movementStep.y;
            float[] candidateStepDownHeights = momentum$collectCandidateStepDownHeights(aabb, colliders, -maxDownStep, stepHeightToSkip);

            for (float candidateStepDHeight : candidateStepDownHeights) {
                Vec3 stepFromGround = collideWithShapes(new Vec3(movement.x, candidateStepDHeight, movement.z), aabb, colliders);
                if (stepFromGround.horizontalDistanceSqr() > 0) {
                    player.addDeltaMovement(
                            new Vec3(
                                    0,
                                    candidateStepDHeight,
                                    0
                            )
                    );
                    cir.setReturnValue(stepFromGround);
                    if (!stateMachine.getCurrentState().getStateType().equals(StateType.SLIDE)) {
                        return;
                    }
                    stateMachine.getContext().setSlopeUnitVector(momentum$getSlopeDirection(player, colliders.getFirst()));
                    setSlideAcceleration(movement, stepFromGround.y, stateMachine);
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

    // 计算坡面单位
    @Unique
    public Vec3 momentum$getSlopeDirection(Player player, VoxelShape collider) {
        AABB bounds = collider.bounds();
        BlockPos feet = BlockPos.containing(
                (bounds.minX + bounds.maxX) / 2,
                bounds.maxY,
                (bounds.minZ + bounds.maxZ) / 2
        );
        BlockGetter level = player.level();
        System.out.println("feet: " + feet);
        System.out.println("player.blockPosition(): " + player.blockPosition());

        float countX = 0;
        float countZ = 0;

        // 先检查四个正方向（2468）
        // 北(2): z-1, 南(8): z+1, 西(4): x-1, 东(6): x+1
        boolean north = momentum$isHigher(level, feet, 0, -1);
        boolean south = momentum$isHigher(level, feet, 0, 1);
        boolean west = momentum$isHigher(level, feet, -1, 0);
        boolean east = momentum$isHigher(level, feet, 1, 0);

        if (north) countZ += 1;  // 北边高，往南推
        if (south) countZ -= 1;  // 南边高，往北推
        if (west) countX += 1;  // 西边高，往东推
        if (east) countX -= 1;  // 东边高，往西推

        // 正方向抵消了才看对角线（1379）
        if (countX == 0 && countZ == 0) {
            boolean nw = momentum$isHigher(level, feet, -1, -1); // 1
            boolean ne = momentum$isHigher(level, feet, 1, -1);  // 3
            boolean sw = momentum$isHigher(level, feet, -1, 1);  // 7
            boolean se = momentum$isHigher(level, feet, 1, 1);   // 9

            // 对角线权重用 0.707 (1/√2)
            float diag = 0.707f;
            if (nw) {
                countX += diag;
                countZ += diag;
            }
            if (ne) {
                countX -= diag;
                countZ += diag;
            }
            if (sw) {
                countX += diag;
                countZ -= diag;
            }
            if (se) {
                countX -= diag;
                countZ -= diag;
            }
        }

        if (countX == 0 && countZ == 0) {
            return Vec3.ZERO;
        }

        return new Vec3(countX, 0, countZ).normalize();
    }

    @Unique
    private boolean momentum$isHigher(BlockGetter level, BlockPos feet, int dx, int dz) {
        BlockPos check = feet.offset(dx, 0, dz);
        // 同层有实心碰撞 = 那边比脚底高
        return !level.getBlockState(check).getCollisionShape(level, check).isEmpty();
    }
}
