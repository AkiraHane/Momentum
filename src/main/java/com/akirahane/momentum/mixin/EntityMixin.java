package com.akirahane.momentum.mixin;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.init.InitAttachments;
import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.floats.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static com.akirahane.momentum.core.MomentumUtils.setSlideAcceleration;

@Mixin(Entity.class)
public abstract class EntityMixin {

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

    protected EntityMixin(EntityType<?> type, Level level) {
    }


    @ModifyVariable(method = "collide", at = @At(value = "STORE", ordinal = 0), name = "stepUpAABB")
    private AABB momentum$modifyStepUpAABB(AABB original, Vec3 movement) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        return original.expandTowards(0, Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))), 0); // 示例
    }

    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collectCandidateStepUpHeights(Lnet/minecraft/world/phys/AABB;Ljava/util/List;FF)[F"))
    private float[] momentum$wrapCollectCandidateStepUpHeights(
            AABB boundingBox, List<VoxelShape> colliders, float maxStepHeight, float stepHeightToSkip, Operation<float[]> original,
            @Local(name = "movement", argsOnly = true) Vec3 movement
    ) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return original.call(boundingBox, colliders, maxStepHeight, stepHeightToSkip);
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original.call(boundingBox, colliders, maxStepHeight, stepHeightToSkip);
        }

        // 修改入参
        float customMaxStep = (float) (maxStepHeight + Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))));


//        float[] result = original.call(boundingBox, colliders, customMaxStep, stepHeightToSkip);
        AABB movementRange = boundingBox.expandTowards(movement.x, 0, movement.z);
        FloatSet candidates = new FloatArraySet(4);

        for (VoxelShape collider : colliders) {
            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // 水平方向是否能碰到
                AABB subBoxFlat = new AABB(minX, movementRange.minY, minZ, maxX, movementRange.maxY, maxZ);
                if (!movementRange.intersects(subBoxFlat)) return;

                float relativeCoord = (float) (maxY - boundingBox.minY);
                if (relativeCoord < 0.0F || relativeCoord == stepHeightToSkip) return;
                if (relativeCoord > customMaxStep) return;

                candidates.add(relativeCoord);
            });
        }

        float[] result = candidates.toFloatArray();
        FloatArrays.unstableSort(result);

        // 过滤返回值
        FloatList filtered = new FloatArrayList();
        float lastHeight = 0.0F;
        for (float current : result) {
            if (current - lastHeight <= this.maxUpStep()) {
                filtered.add(current);
                lastHeight = current;
            } else {
                break;
            }
        }
        result = filtered.toFloatArray();
        float tmp;
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
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
            @Local(name = "entityColliders") List<VoxelShape> entityColliders,
            @Local(name = "movementStep") Vec3 movementStep
    ) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return;
        }
        if (!player.level().isClientSide()) {
            return;
        }
        if (player.isInLiquid()) {
            return;
        }
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return;
        }
        // =================== 内容 ===================

        // 已经上坡则不需要下坡
        if (cir.getReturnValue().y > 0) {
            setSlideAcceleration(movement, cir.getReturnValue().y, stateMachine);
            return;
        }

        movementStep = movement.lengthSqr() == (double) 0.0F ? movement : momentum$collideBoundingBoxDown(
                self, movement, aabb, player.level(), entityColliders
        );

        // 上升或平地时不需要下坡
        if (movementStep.y >= 0) {
            return;
        }
//        // 下落速度超过重力则不下坡
//        if (movement.y < -player.getAttributeValue(Attributes.GRAVITY)) {
//            return;
//        }
        boolean yCollision = movement.y != movementStep.y;
        boolean onGroundAfterCollision = yCollision && movement.y < (double) 0.0F;

//        if (movement.horizontalDistance() > 5) {
//            System.out.println("test");
//        }

        // 步进加速度特殊处理, 原因如下
        // 在stepdown为0.6时,如果当前的速度大于0.5小于1, 下楼梯会出现直接跳过中间小台阶
        // 所需步进为1的情况, 这种情况依旧会向下0.6, 但是由于没有到1所以在目标位置是空中,然后飞出去
        float maxDownStep = (float) (this.maxUpStep() + Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))));
        // 正常行走无法落地才考虑要不要下坡
        if (maxDownStep > 0.0F && (!onGroundAfterCollision) && this.onGround()) {
            entityColliders = this.level.getEntityCollisions(
                    self,
                    aabb.expandTowards(movement.subtract(0, maxDownStep, 0))
            );
            AABB stepDownAABB = aabb.expandTowards(
                    movement.x, -maxDownStep, movement.z
            );
            // 似乎是原版修复浮点精度的, 但是不太理解, 目前不知道去掉会有什么bug, 推测是落地判定相关, 所以先保留
            // 为了能碰撞地面, 所以特地往下拉了一个玩家的身位
            stepDownAABB = stepDownAABB.expandTowards(
                    0.0F, -player.getBoundingBox().getYsize() - 1.0E-5F, 0.0F
            );
            // 获取所有碰撞
            List<VoxelShape> colliders = collectColliders(self, this.level, entityColliders, stepDownAABB);
            float stepHeightToSkip = (float) movementStep.y;
            float[] candidateStepDownHeights = momentum$collectCandidateStepDownHeights(aabb, colliders, -maxDownStep, stepHeightToSkip, this.maxUpStep());

            for (float candidateStepDHeight : candidateStepDownHeights) {
                if (candidateStepDHeight == 0){
                    continue;
                }
                Vec3 stepFromGround = momentum$collideWithShapesDown(new Vec3(movement.x, candidateStepDHeight, movement.z), aabb, colliders);
                if (stepFromGround.horizontalDistanceSqr() > 0) {
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
    private static float[] momentum$collectCandidateStepDownHeights(
            AABB boundingBox, List<VoxelShape> colliders, float maxStepHeight, float stepHeightToSkip,
            float original_StepHeight
    ) {
        FloatSet candidates = new FloatArraySet(4);

        for (VoxelShape collider : colliders) {
            DoubleList coords = collider.getCoords(Direction.Axis.Y);
            // 需要从高到低，所以和原版上坡逻辑是反着的
            for (int i = coords.size() - 1; i >= 0; i--) {
                float relativeCoord = (float) (coords.getDouble(i) - boundingBox.minY);
                if (!(relativeCoord > 0.0F) && relativeCoord != stepHeightToSkip) {
                    if (relativeCoord < maxStepHeight) {
                        break;
                    }

                    candidates.add(relativeCoord);
                }
            }
        }

        float[] sortedCandidates = candidates.toFloatArray();
        FloatArrays.unstableSort(sortedCandidates);
        FloatList filtered = new FloatArrayList();
        float lastHeight = 0.0F;

        for (int i = sortedCandidates.length - 1; i >= 0; i--) {
            float current = sortedCandidates[i];
            if (Math.abs(current - lastHeight) <= original_StepHeight) {
                filtered.add(current);
                lastHeight = current;
            } else {
                break; // 超过单次限制，后面更低的都舍弃
            }
        }
        float[] result = filtered.toFloatArray();
        FloatArrays.unstableSort(result);
        return result;
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

    @Unique
    private static final ImmutableList<Direction.@NotNull Axis> XZY_AXIS_ORDER = ImmutableList.of(Direction.Axis.X, Direction.Axis.Z, Direction.Axis.Y);

    @Unique
    private static final ImmutableList<Direction.@NotNull Axis> ZXY_AXIS_ORDER = ImmutableList.of(Direction.Axis.Z, Direction.Axis.X, Direction.Axis.Y);

    @Unique
    private static Vec3 momentum$collideBoundingBoxDown(@Nullable Entity source, Vec3 movement, AABB boundingBox, Level level, List<VoxelShape> entityColliders) {
        List<VoxelShape> colliders = collectColliders(source, level, entityColliders, boundingBox.expandTowards(movement));
        return momentum$collideWithShapesDown(movement, boundingBox, colliders);
    }


    @Unique
    private static Vec3 momentum$collideWithShapesDown(Vec3 movement, AABB boundingBox, List<VoxelShape> shapes) {
        if (shapes.isEmpty()) {
            return movement;
        } else {
            Vec3 resolvedMovement = Vec3.ZERO;

            for (Direction.Axis axis : momentum$axisStepOrderDown(movement)) {
                double axisMovement = movement.get(axis);
                if (axisMovement != (double) 0.0F) {
                    double collision = Shapes.collide(axis, boundingBox.move(resolvedMovement), shapes, axisMovement);
                    resolvedMovement = resolvedMovement.with(axis, collision);
                }
            }

            return resolvedMovement;
        }
    }

    @Unique
    private static ImmutableList<Direction.@NotNull Axis> momentum$axisStepOrderDown(Vec3 movement) {
        return Math.abs(movement.x) < Math.abs(movement.z) ? XZY_AXIS_ORDER : ZXY_AXIS_ORDER;
    }
}
