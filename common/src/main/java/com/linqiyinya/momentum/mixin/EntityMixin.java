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

    @WrapOperation(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    private void momentum$preventMomentumPush(
            Entity target, double x, double y, double z, Operation<Void> original,
            @Local(argsOnly = true) Entity other
    ) {
        Entity self = (Entity) (Object) this;
        // 原版分别给双方施力；先识别这对实体中的玩家和障碍，两个方向使用相同条件。
        Player player;
        Entity obstacle;
        if (self instanceof Player selfPlayer) {
            player = selfPlayer;
            obstacle = other;
        } else if (other instanceof Player otherPlayer) {
            player = otherPlayer;
            obstacle = self;
        } else {
            original.call(target, x, y, z);
            return;
        }
        if (!player.isCrouching() && !obstacle.isSpectator() && player.canCollideWith(obstacle) &&
                Services.PLATFORM.hasStateMachine(player)) {
            MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
            if (stateMachine.getCurrentState().getStateType() != StateType.ORIGINAL) {
                // 机动模式且非潜行时，跳过玩家与移动障碍实体之间的双向推力。
                return;
            }
        }
        original.call(target, x, y, z);
    }


    // 扩大原版上台阶时收集碰撞体的高度，让一次高速移动能够检查多级台阶。
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
        // movement 是本次请求的位移；取较大的水平分量向上取整，作为额外搜索高度。
        // 这里只扩大查询范围，每一级允许迈上的高度仍由后面的 maxUpStep() 检查。
        return original.expandTowards(0, Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))), 0);
    }

    // 替换原版的候选高度收集：先找顶面高度，再验证能否按行进顺序逐级踩到。
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

        // 候选高度上限与前面扩大的查询范围对应；高度都相对于传入包围盒的脚底。
        float customMaxStep = (float) (maxStepHeight + Math.ceil(Math.max(Math.abs(movement.x), Math.abs(movement.z))));
        if (!colliders.isEmpty()) {
            // 保留原有坡向估算，供滑铲上下坡的加减速使用。
            stateMachine.getContext().setSlopeUnitVector(momentum$getSlopeDirection(player, colliders.getFirst()));
        }

//        float[] result = original.call(boundingBox, colliders, customMaxStep, stepHeightToSkip);
        // expandTowards 得到起点到终点覆盖的包围盒，先用它粗筛可能经过的顶面。
        AABB movementRange = boundingBox.expandTowards(movement.x, 0, movement.z);
        // 不同碰撞箱可能处于同一高度，集合用于去掉重复的候选高度。
        FloatSet candidates = new FloatArraySet(4);

        for (VoxelShape collider : colliders) {
            // 一个方块形状可能包含多个碰撞箱，分别读取各个箱子的顶面。
            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // 将碰撞箱的 Y 范围临时对齐，只比较其 X/Z 范围是否可能经过。
                AABB subBoxFlat = new AABB(minX, movementRange.minY, minZ, maxX, movementRange.maxY, maxZ);
                if (!movementRange.intersects(subBoxFlat)) return;

                // maxY 是顶面世界坐标；减去脚底坐标后才是需要抬升的距离。
                float relativeCoord = (float) (maxY - boundingBox.minY);
                // 跳过脚底以下的顶面，以及原版要求略过的当前碰撞结果高度。
                if (relativeCoord < 0.0F || relativeCoord == stepHeightToSkip) return;
                if (relativeCoord > customMaxStep) return;

                candidates.add(relativeCoord);
            });
        }

        float[] result = candidates.toFloatArray();
        // 从低到高验证，才能检查相邻两级之间的高度差。
        FloatArrays.unstableSort(result);

        FloatList filtered = new FloatArrayList();
        // 记录上一处通过验证的高度，以及它在整段水平位移中的落脚进度。
        float lastHeight = 0.0F;
        double lastLandingProgress = 0;
        for (float current : result) {
            // 候选已经升序排列；这一高度迈不上去，后面更高的高度也不能接上。
            if (current - lastHeight > this.maxUpStep()) {
                break;
            }

            // 用原版碰撞算法试算：抬升到此高度后，能否比未迈台阶时走得更远。
            Vec3 stepFromGround = collideWithShapes(
                    new Vec3(movement.x, current, movement.z),
                    boundingBox,
                    colliders
            );

            if (stepFromGround.horizontalDistanceSqr() <= movementStep.horizontalDistanceSqr()) {
                continue;
            }

            // 要求移动途中有可到达的落脚点；终点可以越过最后一级，处于空中。
            double landingProgress = momentum$findStepUpLanding(
                    boundingBox, movement, current, lastLandingProgress, colliders
            );
            if (landingProgress < 0) {
                // 这一候选缺少有效落脚点，停止继续接更高的台阶。
                break;
            }

            // 这里只保存可用高度；最终采用哪个高度，仍交给原版 collide 决定。
            filtered.add(current);
            lastHeight = current;
            lastLandingProgress = landingProgress;
        }

        result = filtered.toFloatArray();
        // 原版遇到第一个能改善水平移动的高度就返回，倒序让它优先尝试最高可用高度。
        float tmp;
        for (int i = 0, j = result.length - 1; i < j; i++, j--) {
            tmp = result[i];
            result[i] = result[j];
            result[j] = tmp;
        }
        return result;
    }

    // 包装 move 内的 collide 调用，在原版计算完后补充自动下台阶的结果。
    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 momentum$applyStepDown(
            Entity self, Vec3 movement, Operation<Vec3> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        // movement 是请求位移，originalMovement 是经过原版碰撞处理后允许移动的位移。
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

        // 先在临时包围盒上模拟；没有完成任何一级下台阶时，沿用原版结果。
        MomentumStepDownResult result = momentum$stepDown(player, movement);
        if (result == null) {
            return originalMovement;
        }
        // @Share 只在这次 move 调用中共享，供后面的着地、摔落与落地回调使用。
        stepDown.set(result);
        stateMachine.getContext().setSlopeUnitVector(momentum$getSlopeDirection(player, result.support()));
        // dropHeight 用正数累计下降高度，滑铲加速接口用负 Y 表示下坡。
        setSlideAcceleration(movement, -result.dropHeight(), stateMachine, player);
        // 返回总位移后，由原版 move 统一更新实体的真实位置。
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
            // 同步原版的竖直碰撞标记，让后续移动逻辑与最终着地状态一致。
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
        // 向下位移为负数，加回正的 dropHeight，恰好扣除自动下降的部分。
        original.call(self, result == null ? distance : distance + result.dropHeight(), onGround, state, pos);
    }

    @WrapOperation(method = "move", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;updateEntityMovementAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    private void momentum$applyStepDownLanding(
            Block block, BlockGetter level, Entity entity, Operation<Void> original,
            @Share("momentumStepDown") LocalRef<MomentumStepDownResult> stepDown
    ) {
        MomentumStepDownResult result = stepDown.get();
        // 自动下台阶后若已滑到空中，跳过落地回调，避免它把仍应保留的下落速度清零。
        if (result == null || result.onGround()) {
            original.call(block, level, entity);
        }
    }

    @Unique
    private static boolean momentum$hasGroundUnder(AABB movedAABB, List<VoxelShape> colliders) {
        // 复用脚底接触判定：顶面与脚底等高，且水平重叠面积大于零。
        return momentum$findSupportingCollider(movedAABB, colliders) != null;
    }

    // 返回这一高度的可达落脚进度（0 为起点、1 为水平终点）；找不到时返回 -1。
    @Unique
    private static double momentum$findStepUpLanding(
            AABB box, Vec3 movement, double height, double previousProgress, List<VoxelShape> colliders
    ) {
        // 进度只描述水平路径，Y 方向由当前候选高度单独给出。
        Vec3 horizontal = new Vec3(movement.x, 0, movement.z);
        double distance = horizontal.horizontalDistance();
        if (distance <= MOMENTUM_STEP_DOWN_EPSILON) return -1;

        // 先假设脚底已经升到此高度，求沿途会与哪些顶面重叠，再按进入顺序排列。
        List<MomentumSupportInterval> intervals = momentum$collectSupportIntervals(box.move(0, height, 0), horizontal, colliders);
        intervals.sort(Comparator.comparingDouble(MomentumSupportInterval::start));
        for (MomentumSupportInterval interval : intervals) {
            // 这一层的落脚进度不能小于上一层，避免将身后顶面当成前方的中间台阶。
            double start = Math.max(previousProgress, interval.start());
            if (start >= interval.end()) continue;

            // 距离除以水平路程得到进度偏移，让试探点比顶面边缘深入 epsilon。
            // 对很窄的区间最多取到中点，保证试探点仍在顶面重叠范围内。
            double progress = Math.min((start + interval.end()) * 0.5,
                    start + MOMENTUM_STEP_DOWN_EPSILON / distance);
            Vec3 requested = new Vec3(movement.x * progress, height, movement.z * progress);
            // 从原始位置试走到这个落脚点，检查途中是否被侧墙、头顶或其他碰撞体挡住。
            Vec3 reached = collideWithShapes(requested, box, colliders);
            // 任一分量被截短，都说明这个位置不能按请求位移到达。
            if (Math.abs(reached.x - requested.x) > MOMENTUM_STEP_DOWN_EPSILON ||
                    Math.abs(reached.y - height) > MOMENTUM_STEP_DOWN_EPSILON ||
                    Math.abs(reached.z - requested.z) > MOMENTUM_STEP_DOWN_EPSILON) {
                continue;
            }
            // 区间只说明几何上会经过顶面；再检查试走后的脚底确实有支撑。
            if (momentum$hasGroundUnder(box.move(reached), colliders)) {
                return progress;
            }
        }
        // 所有顶面都不可达，当前候选高度不能作为一级有效台阶。
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
            // 回调需要修改累计面积，使用单元素数组保存同一个形状内各碰撞箱的贡献。
            final double[] contactArea = {0.0D};

            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // 只承认恰好接在脚底的顶面，容许 epsilon 范围内的浮点误差。
                if (Math.abs(movedAABB.minY - maxY) > epsilon) {
                    return;
                }

                // 分别计算 X/Z 方向重叠长度；两者都为正才有实际接触面积。
                double overlapX = Math.min(movedAABB.maxX, maxX) - Math.max(movedAABB.minX, minX);
                double overlapZ = Math.min(movedAABB.maxZ, maxZ) - Math.max(movedAABB.minZ, minZ);
                if (overlapX > 0.0D && overlapZ > 0.0D) {
                    contactArea[0] += overlapX * overlapZ;
                }
            });

            // 选接触面积最大的形状，作为坡向计算使用的支撑体。
            if (contactArea[0] > bestContactArea) {
                bestContactArea = contactArea[0];
                best = collider;
            }
        }

        return best;
    }


    // 世界坐标中的容差；用于顶面等高判断、边界查询和跨过边缘时的微小偏移。
    @Unique
    private static final double MOMENTUM_STEP_DOWN_EPSILON = 1.0E-5;

    // movement 是总位移；dropHeight 仅累计自动贴下台阶的正高度；onGround 表示最终是否着地。
    // support 在最终着地时为脚下支撑体，离地时保留最后一级的支撑体供坡向计算使用。
    public record MomentumStepDownResult(Vec3 movement, double dropHeight, boolean onGround, VoxelShape support) {
    }

    // 沿一段水平位移，脚底与某个顶面重叠的进度区间；start/end 均在 [0, 1] 内。
    public record MomentumSupportInterval(double start, double end) {
    }

    @Unique
    private static @Nullable MomentumStepDownResult momentum$stepDown(Player player, Vec3 movement) {
        // box 是模拟位置；AABB.move 返回新包围盒，因此过程不会提前移动真实玩家。
        AABB box = player.getBoundingBox();
        // travelled 累计已完成的水平与贴阶位移，remaining 只保留尚未完成的水平位移。
        Vec3 travelled = Vec3.ZERO;
        Vec3 remaining = new Vec3(movement.x, 0, movement.z);
        // 自然下落不计入 dropHeight，以便后续单独处理摔落距离。
        double dropHeight = 0;
        VoxelShape lastSupport = null;

        // 每轮只走到当前支撑面的边缘，再从新位置检查一级台阶；位移只在最后交给原版应用。
        while (remaining.horizontalDistanceSqr() > MOMENTUM_STEP_DOWN_EPSILON * MOMENTUM_STEP_DOWN_EPSILON) {
            // 覆盖剩余水平路径，并向下搜索一个 maxUpStep；每下降一级后从新高度重新收集。
            List<VoxelShape> colliders = momentum$collectMovementColliders(player,
                    box.expandTowards(remaining.x, -player.maxUpStep(), remaining.z));
            // onGround 是进入移动前的状态，还要确认当前脚底确实存在支撑。
            if (dropHeight == 0 && momentum$findSupportingCollider(box, colliders) == null) {
                return null;
            }

            // 求从当前位置连续有支撑的最远进度：1 表示剩余水平路径都可以走完。
            double supportedUntil = momentum$findSupportEnd(box, remaining, colliders);
            // 稍微跨过边缘，避免浮点误差让刚离开的台阶继续挡住向下碰撞。
            double progress = Math.min(1, supportedUntil + MOMENTUM_STEP_DOWN_EPSILON / remaining.horizontalDistance());
            Vec3 requested = remaining.scale(progress);
            // 即使脚下连续有支撑，也必须保留侧墙等障碍对水平位移的限制。
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
                // 仍有支撑时先处理剩余水平位移；已经走完则会在下一次循环条件处结束。
                lastSupport = support;
                continue;
            }

            // 从实际脚底向下碰撞，只允许下降一个上台阶高度；内部碰撞箱不能提供虚构的中间落脚点。
            double down = Shapes.collide(Direction.Axis.Y, box, colliders, -player.maxUpStep());
            AABB landingBox = box.move(0, down, 0);
            // down 为负且落点确实接触顶面，才算成功下了一级；仅向下走满距离不代表有地面。
            VoxelShape landingSupport = down < -MOMENTUM_STEP_DOWN_EPSILON
                    ? momentum$findSupportingCollider(landingBox, colliders) : null;
            if (landingSupport == null) {
                if (dropHeight == 0) {
                    // 还没有完成任何自动下台阶，调用者可以直接采用先前算好的原版位移。
                    return null;
                }

                // 下一落差过大时保留已完成的台阶移动，剩余位移及原本的下落交回普通碰撞处理。
                Vec3 falling = remaining.add(0, movement.y, 0);
                // 正常下落范围可能与单级台阶查询不同，因此按这次请求重新收集碰撞体。
                List<VoxelShape> fallingColliders = momentum$collectMovementColliders(player, box.expandTowards(falling));
                Vec3 normalMovement = collideWithShapes(falling, box, fallingColliders);
                VoxelShape finalSupport = momentum$findSupportingCollider(box.move(normalMovement), fallingColliders);
                // 向下位移被截短且脚底有支撑，才将本次普通下落判为落地。
                boolean onGround = normalMovement.y != falling.y && finalSupport != null;
                return new MomentumStepDownResult(travelled.add(normalMovement), dropHeight, onGround,
                        finalSupport == null ? lastSupport : finalSupport);
            }

            // 确认下一级后更新模拟脚底，再从该高度继续处理剩余水平路程。
            box = landingBox;
            travelled = travelled.add(0, down, 0);
            // down 为负，减去它得到正的累计贴阶高度。
            dropHeight -= down;
            lastSupport = landingSupport;
        }

        // 走完整段水平路径后，只有确实下降过才提供修正结果；平地移动仍采用原版结果。
        return dropHeight == 0 ? null : new MomentumStepDownResult(travelled, dropHeight, true, lastSupport);
    }

    @Unique
    private static List<VoxelShape> momentum$collectMovementColliders(Player player, AABB range) {
        // 包含刚好贴着脚底的碰撞面，否则碰撞查询可能漏掉边界上的支撑方块。
        AABB search = range.inflate(MOMENTUM_STEP_DOWN_EPSILON);
        // 合并范围内的实体、方块和世界边界碰撞体，交给后续各次试算复用。
        return collectColliders(player, player.level(), player.level().getEntityCollisions(player, search), search);
    }

    @Unique
    private static double momentum$findSupportEnd(AABB box, Vec3 movement, List<VoxelShape> colliders) {
        // 每个区间表示一个顶面能在什么进度内支撑脚底。
        List<MomentumSupportInterval> intervals = momentum$collectSupportIntervals(box, movement, colliders);

        // 拼接从当前位置连续可踩到的顶面，遇到真正的空隙就停止，后面的地面不能跨空接上。
        intervals.sort(Comparator.comparingDouble(MomentumSupportInterval::start));
        double supportedUntil = 0;
        // 将世界坐标中的容差转换为本段位移的进度容差。
        double tolerance = MOMENTUM_STEP_DOWN_EPSILON / movement.horizontalDistance();
        for (MomentumSupportInterval interval : intervals) {
            // 当前连续区间接不上下一个顶面，缺口起点就是需要尝试下台阶的位置。
            if (interval.start() > supportedUntil + tolerance) break;
            // 重叠或相接的顶面合并，玩家同时踩在多个方块上时可以连续前进。
            supportedUntil = Math.max(supportedUntil, interval.end());
        }
        return supportedUntil;
    }

    @Unique
    private static List<MomentumSupportInterval> momentum$collectSupportIntervals(AABB box, Vec3 movement, List<VoxelShape> colliders) {
        List<MomentumSupportInterval> intervals = new ArrayList<>();
        for (VoxelShape collider : colliders) {
            collider.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                // 只收集当前脚底高度的顶面；其他高度要等上坡或下坡试算时再检查。
                if (Math.abs(box.minY - maxY) > MOMENTUM_STEP_DOWN_EPSILON) {
                    return;
                }

                // 用 t 表示进度，脚底沿 box + movement * t 移动，初始有效范围为整段 [0, 1]。
                double start = 0;
                double end = 1;
                if (movement.x == 0) {
                    // X 不动时必须已与顶面重叠，否则整段移动都不可能踩到它。
                    if (box.minX >= maxX || box.maxX <= minX) return;
                } else {
                    // 分别求脚底前后边界经过顶面两侧边界的时刻。
                    double first = (minX - box.maxX) / movement.x;
                    double second = (maxX - box.minX) / movement.x;
                    // 负向移动会颠倒两个时刻的顺序，先取 min/max 再与当前区间求交集。
                    start = Math.max(start, Math.min(first, second));
                    end = Math.min(end, Math.max(first, second));
                }
                // Z 方向同样求重叠区间，再与 X 方向取交集，确保同一时刻两个方向都重叠。
                if (movement.z == 0) {
                    if (box.minZ >= maxZ || box.maxZ <= minZ) return;
                } else {
                    double first = (minZ - box.maxZ) / movement.z;
                    double second = (maxZ - box.minZ) / movement.z;
                    start = Math.max(start, Math.min(first, second));
                    end = Math.min(end, Math.max(first, second));
                }
                // start == end 只有边线接触；保留有实际重叠宽度的区间。
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
