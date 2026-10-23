package com.linqiyinya.momentum.mixin;

import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.platform.Services;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.floats.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.linqiyinya.momentum.core.utils.SlideUtils.setSlideAcceleration;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    private static List<VoxelShape> collectColliders(@Nullable Entity source, Level level, List<VoxelShape> entityColliders, AABB boundingBox) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    public abstract float maxUpStep();

    @Shadow
    private static Vec3 collideWithShapes(Vec3 movement, AABB boundingBox, List<VoxelShape> shapes){
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    protected EntityMixin(EntityType<?> type, Level level) {
    }


    @ModifyVariable(method = "collide", at = @At(value = "STORE", ordinal = 0), name = "stepUpAABB")
    private AABB momentum$modifyStepUpAABB(AABB original, Vec3 movement) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return original;
        }
        if (!player.level().isClientSide()) {
            return original;
        }
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original;
        }
        return original.expandTowards(0, Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))), 0); // 示例
    }

    @WrapOperation(method = "collide", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collectCandidateStepUpHeights(Lnet/minecraft/world/phys/AABB;Ljava/util/List;FF)[F"))
    private float[] momentum$wrapCollectCandidateStepUpHeights(
            AABB boundingBox, List<VoxelShape> colliders, float maxStepHeight, float stepHeightToSkip, Operation<float[]> original,
            @Local(name = "movement", argsOnly = true) Vec3 movement,
            @Local(name = "movementStep") Vec3 movementStep
    ) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player player)) {
            return original.call(boundingBox, colliders, maxStepHeight, stepHeightToSkip);
        }
        if (!player.level().isClientSide()) {
            return original.call(boundingBox, colliders, maxStepHeight, stepHeightToSkip);
        }
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return original.call(boundingBox, colliders, maxStepHeight, stepHeightToSkip);
        }

        // 修改入参
        float customMaxStep = (float) (maxStepHeight + Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))));
        if (!colliders.isEmpty()) {
            stateMachine.getContext().setSlopeUnitVector(momentum$getSlopeDirection(player, colliders.getFirst()));
        }

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
        double lastLandingProgress = 0;
        for (float current : result) {
            if (current - lastHeight > this.maxUpStep()) {
                break;
            }

            Vec3 stepFromGround = collideWithShapes(
                    new Vec3(movement.x, current, movement.z),
                    boundingBox,
                    colliders
            );

            if (stepFromGround.horizontalDistanceSqr() <= movementStep.horizontalDistanceSqr()) {
                continue;
            }

            // 每一级在移动途中有真实落脚点即可；剩余位移可以让玩家越过楼梯顶端。
            double landingProgress = momentum$findStepUpLanding(
                    boundingBox, movement, current, lastLandingProgress, colliders
            );
            if (landingProgress < 0) {
                break;
            }

            filtered.add(current);
            lastHeight = current;
            lastLandingProgress = landingProgress;
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

    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 momentum$applyStepDown(
            Entity self, Vec3 movement, Operation<Vec3> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        Vec3 originalMovement = original.call(self, movement);
        if (!(self instanceof Player player)) {
            return originalMovement;
        }
        if (!player.level().isClientSide() || player.isInLiquid()) {
            return originalMovement;
        }
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return originalMovement;
        }

        // 上坡沿用原来的结果；下坡只从仍然着地、正在向下移动的玩家开始。
        if (originalMovement.y > 0) {
            setSlideAcceleration(movement, originalMovement.y, stateMachine, player);
            return originalMovement;
        }
        if (!player.onGround() || movement.y >= 0 || player.maxUpStep() <= 0 ||
                movement.horizontalDistanceSqr() == 0) {
            return originalMovement;
        }

        MomentumStepDownResult result = momentum$stepDown(player, movement);
        if (result == null) {
            return originalMovement;
        }
        stepDown.set(result);
        stateMachine.getContext().setSlopeUnitVector(momentum$getSlopeDirection(player, result.support()));
        setSlideAcceleration(movement, -result.dropHeight(), stateMachine, player);
        return result.movement();
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setOnGroundWithMovement(ZZLnet/minecraft/world/phys/Vec3;)V"))
    private void momentum$applyStepDownGroundState(
            Entity self, boolean onGround, boolean horizontalCollision, Vec3 movement, Operation<Void> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        MomentumStepDownResult result = stepDown.get();
        if (result != null) {
            // 台阶修正也会改变 Y 位移，必须用最终落脚情况判断落地，避免滑出悬崖后仍被当成着地。
            onGround = result.onGround();
            self.verticalCollision = onGround;
            self.verticalCollisionBelow = onGround;
        }
        original.call(self, onGround, horizontalCollision, movement);
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;checkFallDamage(DZLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V"))
    private void momentum$excludeStepDownFromFallDistance(
            Entity self, double distance, boolean onGround, BlockState state, BlockPos pos, Operation<Void> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        MomentumStepDownResult result = stepDown.get();
        // 自动贴下台阶的距离不计入摔落；越过最后一级后的正常下落仍由原版累计。
        original.call(self, result == null ? distance : distance + result.dropHeight(), onGround, state, pos);
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;updateEntityMovementAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    private void momentum$applyStepDownLanding(
            Block block, BlockGetter level, Entity entity, Operation<Void> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        MomentumStepDownResult result = stepDown.get();
        if (result == null || result.onGround()) {
            original.call(block, level, entity);
        }
    }

    @Unique
    private static boolean momentum$hasGroundUnder(AABB movedAABB, List<VoxelShape> colliders) {
        return momentum$findSupportingCollider(movedAABB, colliders) != null;
    }

    @Unique
    private static double momentum$findStepUpLanding(
            AABB box, Vec3 movement, double height, double previousProgress, List<VoxelShape> colliders
    ) {
        Vec3 horizontal = new Vec3(movement.x, 0, movement.z);
        double distance = horizontal.horizontalDistance();
        if (distance <= MOMENTUM_STEP_DOWN_EPSILON) return -1;

        // 在这一高度寻找脚底会经过的顶面，按行进顺序检查，保留每一级对应的位置。
        List<MomentumSupportInterval> intervals = momentum$collectSupportIntervals(box.move(0, height, 0), horizontal, colliders);
        intervals.sort(Comparator.comparingDouble(MomentumSupportInterval::start));
        for (MomentumSupportInterval interval : intervals) {
            double start = Math.max(previousProgress, interval.start());
            if (start >= interval.end()) continue;

            // 取刚进入顶面的位置，避免仅接触边线；窄顶面也必须选在实际重叠范围内。
            double progress = Math.min((start + interval.end()) * 0.5,
                    start + MOMENTUM_STEP_DOWN_EPSILON / distance);
            Vec3 requested = new Vec3(movement.x * progress, height, movement.z * progress);
            Vec3 reached = collideWithShapes(requested, box, colliders);
            if (Math.abs(reached.x - requested.x) > MOMENTUM_STEP_DOWN_EPSILON ||
                    Math.abs(reached.y - height) > MOMENTUM_STEP_DOWN_EPSILON ||
                    Math.abs(reached.z - requested.z) > MOMENTUM_STEP_DOWN_EPSILON) {
                continue;
            }
            if (momentum$hasGroundUnder(box.move(reached), colliders)) {
                return progress;
            }
        }
        return -1;
    }

    /**
     * 找到实际承受玩家脚底的碰撞体。碰撞列表顺序随包围盒向正/负坐标扩展而变化，不能使用 getFirst()。
     * 当脚底同时接触多个形状时，选择水平接触面积最大的一个，避免在台阶边缘产生方向抖动。
     */
    @Unique
    private static VoxelShape momentum$findSupportingCollider(AABB movedAABB, List<VoxelShape> colliders) {
        double epsilon = 1.0E-5D;
        VoxelShape best = null;
        double bestContactArea = 0.0D;

        for (VoxelShape collider : colliders) {
            final double[] contactArea = {0.0D};

            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                if (Math.abs(movedAABB.minY - maxY) > epsilon) {
                    return;
                }

                double overlapX = Math.min(movedAABB.maxX, maxX) - Math.max(movedAABB.minX, minX);
                double overlapZ = Math.min(movedAABB.maxZ, maxZ) - Math.max(movedAABB.minZ, minZ);
                if (overlapX > 0.0D && overlapZ > 0.0D) {
                    contactArea[0] += overlapX * overlapZ;
                }
            });

            if (contactArea[0] > bestContactArea) {
                bestContactArea = contactArea[0];
                best = collider;
            }
        }

        return best;
    }


    @Unique
    private static final double MOMENTUM_STEP_DOWN_EPSILON = 1.0E-5;

    public record MomentumStepDownResult(Vec3 movement, double dropHeight, boolean onGround, VoxelShape support) {
    }

    public record MomentumSupportInterval(double start, double end) {
    }

    @Unique
    private static @Nullable MomentumStepDownResult momentum$stepDown(Player player, Vec3 movement) {
        AABB box = player.getBoundingBox();
        Vec3 travelled = Vec3.ZERO;
        Vec3 remaining = new Vec3(movement.x, 0, movement.z);
        double dropHeight = 0;
        VoxelShape lastSupport = null;

        // 每轮只走到当前支撑面的边缘，再从新位置检查一级台阶；位移只在最后交给原版应用。
        while (remaining.horizontalDistanceSqr() > MOMENTUM_STEP_DOWN_EPSILON * MOMENTUM_STEP_DOWN_EPSILON) {
            List<VoxelShape> colliders = momentum$collectMovementColliders(player,
                    box.expandTowards(remaining.x, -player.maxUpStep(), remaining.z));
            if (dropHeight == 0 && momentum$findSupportingCollider(box, colliders) == null) {
                return null;
            }

            double supportedUntil = momentum$findSupportEnd(box, remaining, colliders);
            // 稍微跨过边缘，避免浮点误差让刚离开的台阶继续挡住向下碰撞。
            double progress = Math.min(1, supportedUntil + MOMENTUM_STEP_DOWN_EPSILON / remaining.horizontalDistance());
            Vec3 requested = remaining.scale(progress);
            Vec3 horizontal = collideWithShapes(requested, box, colliders);
            box = box.move(horizontal);
            travelled = travelled.add(horizontal);

            // 被墙挡住的分量按原版停止，其余方向保留尚未走完的位移。
            remaining = new Vec3(
                    horizontal.x == requested.x ? remaining.x - horizontal.x : 0,
                    0,
                    horizontal.z == requested.z ? remaining.z - horizontal.z : 0
            );
            VoxelShape support = momentum$findSupportingCollider(box, colliders);
            if (support != null) {
                lastSupport = support;
                continue;
            }

            // 从实际脚底向下碰撞，只允许下降一个上台阶高度；内部碰撞箱不能提供虚构的中间落脚点。
            double down = Shapes.collide(Direction.Axis.Y, box, colliders, -player.maxUpStep());
            AABB landingBox = box.move(0, down, 0);
            VoxelShape landingSupport = down < -MOMENTUM_STEP_DOWN_EPSILON
                    ? momentum$findSupportingCollider(landingBox, colliders) : null;
            if (landingSupport == null) {
                if (dropHeight == 0) {
                    return null;
                }

                // 下一落差过大时保留已完成的台阶移动，剩余位移及原本的下落交回普通碰撞处理。
                Vec3 falling = remaining.add(0, movement.y, 0);
                List<VoxelShape> fallingColliders = momentum$collectMovementColliders(player, box.expandTowards(falling));
                Vec3 normalMovement = collideWithShapes(falling, box, fallingColliders);
                VoxelShape finalSupport = momentum$findSupportingCollider(box.move(normalMovement), fallingColliders);
                boolean onGround = normalMovement.y != falling.y && finalSupport != null;
                return new MomentumStepDownResult(travelled.add(normalMovement), dropHeight, onGround,
                        finalSupport == null ? lastSupport : finalSupport);
            }

            box = landingBox;
            travelled = travelled.add(0, down, 0);
            dropHeight -= down;
            lastSupport = landingSupport;
        }

        return dropHeight == 0 ? null : new MomentumStepDownResult(travelled, dropHeight, true, lastSupport);
    }

    @Unique
    private static List<VoxelShape> momentum$collectMovementColliders(Player player, AABB range) {
        // 包含刚好贴着脚底的碰撞面，否则碰撞查询可能漏掉边界上的支撑方块。
        AABB search = range.inflate(MOMENTUM_STEP_DOWN_EPSILON);
        return collectColliders(player, player.level(), player.level().getEntityCollisions(player, search), search);
    }

    @Unique
    private static double momentum$findSupportEnd(AABB box, Vec3 movement, List<VoxelShape> colliders) {
        List<MomentumSupportInterval> intervals = momentum$collectSupportIntervals(box, movement, colliders);

        // 拼接从当前位置连续可踩到的顶面，遇到真正的空隙就停止，后面的地面不能跨空接上。
        intervals.sort(Comparator.comparingDouble(MomentumSupportInterval::start));
        double supportedUntil = 0;
        double tolerance = MOMENTUM_STEP_DOWN_EPSILON / movement.horizontalDistance();
        for (MomentumSupportInterval interval : intervals) {
            if (interval.start() > supportedUntil + tolerance) break;
            supportedUntil = Math.max(supportedUntil, interval.end());
        }
        return supportedUntil;
    }

    @Unique
    private static List<MomentumSupportInterval> momentum$collectSupportIntervals(AABB box, Vec3 movement, List<VoxelShape> colliders) {
        List<MomentumSupportInterval> intervals = new ArrayList<>();
        for (VoxelShape collider : colliders) {
            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                if (Math.abs(box.minY - maxY) > MOMENTUM_STEP_DOWN_EPSILON) {
                    return;
                }

                // 求脚底与这个顶面在本段移动中重叠的起止进度；X、Z 必须同时重叠。
                double start = 0;
                double end = 1;
                if (movement.x == 0) {
                    if (box.minX >= maxX || box.maxX <= minX) return;
                } else {
                    double first = (minX - box.maxX) / movement.x;
                    double second = (maxX - box.minX) / movement.x;
                    start = Math.max(start, Math.min(first, second));
                    end = Math.min(end, Math.max(first, second));
                }
                if (movement.z == 0) {
                    if (box.minZ >= maxZ || box.maxZ <= minZ) return;
                } else {
                    double first = (minZ - box.maxZ) / movement.z;
                    double second = (maxZ - box.minZ) / movement.z;
                    start = Math.max(start, Math.min(first, second));
                    end = Math.min(end, Math.max(first, second));
                }
                if (start < end) {
                    intervals.add(new MomentumSupportInterval(start, end));
                }
            });
        }

        return intervals;
    }

    // 计算坡面单位
    @Unique
    public Vec3 momentum$getSlopeDirection(Player player, VoxelShape collider) {
        if (collider.isEmpty()) return Vec3.ZERO;
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

}
