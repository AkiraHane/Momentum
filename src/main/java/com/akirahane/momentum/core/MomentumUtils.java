package com.akirahane.momentum.core;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import static com.akirahane.momentum.core.effect.MomentumEffectType.ACCELERATION;

public class MomentumUtils {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    private static final Identifier BOOSTER_SPEED_ID = Identifier.fromNamespaceAndPath(Momentum.MODID, "booster_speed");
    private static final Identifier BOOSTER_JUMP_ID = Identifier.fromNamespaceAndPath(Momentum.MODID, "booster_jump");
    private static final Identifier BOOSTER_STEP_ID = Identifier.fromNamespaceAndPath(Momentum.MODID, "booster_step");
    // 滑行最大速度上限（格/tick）
    public static final double MAX_SLIDE_SPEED = 0.98;
    // 下坡基础加速系数
    public static final float DOWNHILL_ACCEL_FACTOR = 0.08F;
    // 上坡减速系数
    public static final float UPHILL_DECEL_FACTOR = 0.12F;


    // 滑行上下坡加速和减速
    public static void setSlideAcceleration(Vec3 movement, double movementStepY, MovementStateMachine stateMachine, Player player) {

        if (!stateMachine.getCurrentState().getStateType().equals(StateType.SLIDE)) {
            return;
        }


        double currentSpeed = movement.horizontalDistance();
        stateMachine.getContext().setBlockStep((movementStepY + stateMachine.getContext().getBlockStep()) / 2);

        // duration 固定为 1 tick，因为每次阶梯变化都会重新调用
        // 避免低速时 duration 过长导致效果堆积
        int duration = 2; // 1 + 1 用于下一 tick 处理

        // === 下坡（movementStepY < 0）===
        Vec3 slopeDir = stateMachine.getContext().getSlopeUnitVector();
        if (movementStepY < 0) {
            double dropHeight = Math.abs(movementStepY);

            // 速度越接近上限，加速越小（渐近式衰减）
            // speedRatio: 0 → 刚起步，1 → 已达上限
            double speedRatio = Math.min(1.0, currentSpeed / MAX_SLIDE_SPEED);
            // 剩余加速空间，越快加速越少
            double headroom = 1.0 - speedRatio * speedRatio;

            // speed与dropHeight的匹配度: 当speed≈dropHeight时最优（共振效应）
            double matchRatio = Math.min(currentSpeed, dropHeight) / Math.max(Math.max(currentSpeed, dropHeight), 0.001);
            // 加速值 = 落差产生的势能转化 × 剩余空间 × 匹配度
            float acceleration = (float) (DOWNHILL_ACCEL_FACTOR * dropHeight * 3 * headroom * matchRatio);
            stateMachine.getContext().SLIDE_ACCELERATION.setValue(new Vec3(slopeDir.x * acceleration, 0, slopeDir.z * acceleration));
            stateMachine.getContext().addEffect(ACCELERATION, stateMachine.getContext().SLIDE_ACCELERATION, duration);
//            stateMachine.getContext().addEffect(MomentumEffectType.BLOCK_FRICTION, stateMachine.getContext().SLIDE_BLOCK_FRICTION, duration * 2);

            // === 上坡（movementStepY > 0）===
        } else if (movementStepY > 0) {
            double riseHeight = Math.abs(movementStepY);

            // 上坡减速：与坡度成正比，但用 sqrt 软化手感
            // 不会一步减到 0，保留滑行惯性感
            float deceleration = (float) (UPHILL_DECEL_FACTOR * Math.min(riseHeight * riseHeight, 1.0));

            stateMachine.getContext().SLIDE_ACCELERATION.setValue(new Vec3(
                    slopeDir.x * deceleration,
                    0,
                    slopeDir.z * deceleration));
            stateMachine.getContext().addEffect(ACCELERATION, stateMachine.getContext().SLIDE_ACCELERATION, duration);
        }
    }

    public static void applyBoosterAttributes(Player player, boolean apply) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (speed == null || jump == null || step == null) return;

        speed.removeModifier(BOOSTER_SPEED_ID);
        jump.removeModifier(BOOSTER_JUMP_ID);
        step.removeModifier(BOOSTER_STEP_ID);

        if (apply) {
            speed.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_SPEED_ID, 0.04, AttributeModifier.Operation.ADD_VALUE));
            jump.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_JUMP_ID, 0.2, AttributeModifier.Operation.ADD_VALUE));
            step.addOrReplacePermanentModifier(new AttributeModifier(BOOSTER_STEP_ID, 0.4, AttributeModifier.Operation.ADD_VALUE));
        }
    }


    public static boolean isDivingEdge(Player player, PlayerMovementContext context) {
        Vec3 movement = player.getDeltaMovement();
        int dx = (int) Math.signum(movement.x);
        int dz = (int) Math.signum(movement.z);
        if (dx == 0 && dz == 0) return false;

        BlockPos center = player.blockPosition().offset(dx * 2, 0, dz * 2);
        Level level = player.level();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y >= -3; y--) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    // 有碰撞箱 = 有实体方块，不是跳水边缘
                    if (!state.getCollisionShape(level, pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean canPlayerFitAtPose(Player player, Pose pose) {
        EntityDimensions dimensions = player.getDimensions(pose);
        AABB boundingBox = dimensions.makeBoundingBox(player.position());
        return player.level().noCollision(player, boundingBox);
    }

    /**
     * 指数趋近：current 以 alpha 比例向 target 收敛（等价于 Mth.lerp(alpha, current, target)）。
     * 剩余误差 <= epsilon 时直接吸附到 target，避免浮点下永远达不到。
     * 用于摄像机 roll / FOV / 手臂偏移 / 墙跑速度等"平滑逼近目标值"场景。
     */
    public static float approach(float current, float target, float alpha, float epsilon) {
        float next = current + (target - current) * alpha;
        return Math.abs(next - target) <= epsilon ? target : next;
    }

}
