package com.akirahane.momentum.core.context;

import com.akirahane.momentum.client.MomentumClient;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.init.InitItems;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationController;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.util.*;
import java.util.function.Function;

import static com.akirahane.momentum.client.animation.MomentumAnimationController.MAX_SAFE_SPEED;
import static com.akirahane.momentum.core.MomentumUtils.applyBoosterAttributes;
import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.*;

@Getter
@Setter
public class PlayerMovementContext {
    // 静态量
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 键位
    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, JUMP = 4, SPRINT = 5, SHIFT = 6, SHIFT_HOLD = 7;
    // 音效
//    HIT(SoundType::getHitSound),
//    BREAK(SoundType::getBreakSound),
//    PLACE(SoundType::getPlaceSound),
//    FALL(SoundType::getFallSound);
    public static final Function<SoundType, SoundEvent> STEP = SoundType::getStepSound,
            HIT = SoundType::getHitSound,
            BREAK = SoundType::getBreakSound,
            PLACE = SoundType::getPlaceSound,
            FALL = SoundType::getFallSound;
    // 动画控制器
    MomentumAnimationController controller;
    // 动画moLang
    MochaEngine<AnimationController> mocha;

    // 标志位
    // 是否装备喷射器
    private boolean hasJetBooster = false;
    // 是否能进行机动
    private boolean canMomentum = true;
    // 是否不接受移动输入
    private boolean noMoveInput = false;
    // 是否禁止跳跃
    private boolean noJump = false;
    // 是否有边缘
    private boolean hasLedge = false;
    // 速降补偿找到ledge时的Y偏移量（用于进入WallHang时修正位置）
    private double ledgeOffsetY = 0;
    // 眼睛到下巴的前方是否有可抓取墙壁
    private boolean hasFaceWall = false;
    // 是否双击UP DOWN LEFT RIGHT JUMP
    private boolean doubleClickUp = false;
    private boolean doubleClickDown = false;
    private boolean doubleClickLeft = false;
    private boolean doubleClickRight = false;
    private boolean doubleClickJump = false;
    private boolean doubleClickSprint = false;
    // 是否进入受身
    private boolean toBreakFallState = false;
    // 当前是否轮到左脚跳
    private boolean leftFootJump = true;


    // 移动速度
    private Vec3 speed = Vec3.ZERO;
    // 上一Tick移动速度
    private Vec3 oldSpeed = Vec3.ZERO;
    // 上一Tick的DeltaMovement
    private Vec3 oldDeltaMovement = Vec3.ZERO;
    // 滑行上下单位高度
    private double blockStep = 0;
    // 坡度加速向量
    private Vec3 slopeUnitVector = Vec3.ZERO;
    // 上次掉落的数据
    private double lastFallDistance = 0;
    // 闪避无敌时间
    private final int dodgeInvincible = 10;
    // 移动向量
    private Vec3 inputVec = Vec3.ZERO;
    // 向墙的法向量
    Vec3 wallNormal = Vec3.ZERO;
    // 墙跑使用的法向量
    Vec3 runWallNormal = Vec3.ZERO;
    // 向墙视线角度
    float lookWallAngle = 360F;
    // 向墙输入角度
    float inputWallAngle = 360F;
    // 跳跃最高速度(到达这个速度停止继续加速, 但不会减速)
    private double jumpLimitSpeed = 0;
    // 跳跃加速强度(同时吃速度属性和跳跃提升属性)
    private double jumpAcceleration = 0;
    // 连跳阻止加速持续时间
    private int jumpCooldown = 0;
    // 墙面方块摩擦力
    private float wallFriction = 0.6f;
    // need sound tick = 0
    private float needSoundTick = 0;
    // 当前墙面重力变化倍率
    private float gravityModify = 0;
    // 跳跃动画播放速度
    private float jumpAnimationSpeed = 1;
    // 头身角度差
    private float bodyHeadAngleDiff = 0F;
    private float prevBodyHeadAngleDiff = 0F;
    private float prevHeadXRot = 0F;

    private int debugFrameCalls = 0;
    private int debugTickCounter = 0;

    // 摄像头旋转角度
    private float targetCameraRoll = 0F;
    private float currentCameraRoll = 0F;
    private float prevCameraRoll = 0F;

    private float targetFovBonus = 0F;
    private float currentFovBonus = 0F;
    private float prevFovBonus = 0F;

    private float momentumRollIntensity = 0F;  // 当前动作的最大倾斜角度
    private float currentMomentumRoll = 0F;
    private float prevMomentumRoll = 0F;

    private float armOffsetY = 0F;
    private float prevArmOffsetY = 0F;
    private float targetArmOffsetY = 0F;

    private float armRotX = 0F;
    private float prevArmRotX = 0F;
    private float targetArmRotX = 0F;

    // 彩蛋随机数(1~100)
    private int luckyNumber = 0;


    // 当前播放的动画名称
    private String currentAnimationName = null;


    // 效果合计
    // 输入缓冲角标
    private int inputBufferIndex = 0;
    // 输入缓冲大小
    private final int inputBufferSize = 10;
    // 输入缓冲
    private final Map<MomentumEffectType, Set<MomentumEffect>> pendingEffectPool = new HashMap<>();
    @SuppressWarnings("unchecked")
    private final HashSet<Integer>[] inputBuffer = new HashSet[inputBufferSize];
    // 墙面方块列表
    private final List<BlockPos> wallBlocks = new ArrayList<>();

    // 每tick要变动的效果
    // 跳跃计数 用于跳跃限速
    private int jumpTimer = 0;
    // 滑铲冷却
    private int slideCooldown = 0;
    // 滑铲冷却刚完成（用于触发提示）
    private boolean slideJumpCooldownJustFinished = false;
    // 受身计时器
    private int breakFallTimer = 0;
    // 闪避计时器
    private int dodgeTimer = 0;
    // 闪避冷却计时器
    private int dodgeCooldown = 0;
    // 状态转换附加数据 (用于客户端→服务端传递客户端独有的信息, 如 Dodge 方向)
    // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT, -1=无数据
    private int transitionExtraData = -1;
    // 墙面同步数据 (bit 0-2: wallNormal索引, bit 3: inputWallAngle左右标志; -1=无墙面数据)
    private byte transitionWallData = -1;
    // 翻越计时器
    private int vaultTimer = 0;
    // 受身计数器
    private int breakFallReadyCount = -1;
    // 墙跳计时器
    private int wallJumpTimer = 0;
    // 游泳推进计时器
    private int swimPushTimer = 0;
    // 墙跳加速冷却
    private int wallKickCooldown = 0;

    // 状态中进行变动的数值
    // 滞空计时器
    private int airborneTimer = 0;

    // 机动模式总阻力
    public static MomentumEffect DEFAULT_FRICTION = new MomentumEffect(
            new Vec3(0.9, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );

    // 翻滚breakfall总阻力
    public MomentumEffect BREAK_FALL_FRICTION = new MomentumEffect(
            new Vec3(0.5, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            0
    );

    // 闪避方块摩擦力
    public MomentumEffect DODGE_BLOCK_FRICTION = new MomentumEffect(
            new Vec3(0, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            0
    );

    // 滑铲总阻力
    public MomentumEffect SLIDE_FRICTION = new MomentumEffect(
            new Vec3(0.1, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );
    // 滑铲方块阻力
    public MomentumEffect SLIDE_BLOCK_FRICTION = new MomentumEffect(
            Vec3.ZERO,
            Vec3.ZERO,
            MULTIPLIER,
            0
    );
    // 滑铲加速
    public MomentumEffect SLIDE_ACCELERATION = new MomentumEffect(
            Vec3.ZERO,
            Vec3.ZERO,
            COMPOSE,
            0
    );

    public static MomentumEffect AIR_ACCELERATION = new MomentumEffect(
            new Vec3(0.06, 1, 0.06),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );

    public static MomentumEffect AIR_LIMIT_ACCELERATION = new MomentumEffect(
            new Vec3(0.3, Float.MAX_VALUE, 0.3),
            Vec3.ZERO,
            COMPOSE,
            -1
    );

    // 挂墙移动阻力
    public static MomentumEffect WALL_FRICTION = new MomentumEffect(
            new Vec3(0.4, 0, 0),
            Vec3.ZERO,
            COMPOSE,
            -1
    );

    public PlayerMovementContext(Player player) {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            pendingEffectPool.put(type, new HashSet<>());
        }
        for (int i = 0; i < inputBufferSize; i++) {
            inputBuffer[i] = new HashSet<>();
        }
        if (player.level().isClientSide()) {
            controller = (MomentumAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                    player, MomentumClient.MOVEMENT_ANIM
            );
            if (controller != null) {
                mocha = controller.getMolangRuntime();
                bindVariables();
            }
        }
    }

    private void bindVariables() {
        Value value = this.mocha.scope().get("math");
        if (value instanceof ObjectValue math) {
            // clamp
            math.setFunction("clamp", Mth::clamp);
            // min
            math.setFunction("min", Math::min);
            math.setFunction("atan2", (y, x) -> (float) Math.toDegrees(Mth.atan2(y, x)));
        } else {
            LOGGER.warn("Failed to bind math.min_angle_180 to Mocha");
        }
        value = this.mocha.scope().get("variable");
        if (value instanceof ObjectValue variable) {
            final Minecraft mc = Minecraft.getInstance();

            variable.setFunction("get_movement_speed", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, (float) oldSpeed.horizontalDistance(),
                        (float) getSpeed().horizontalDistance());
            });
            variable.setFunction("get_movement_y_speed", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, (float) oldSpeed.y, (float) getSpeed().y());
            });
            variable.setFunction("stable_body_head_angle", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                return Mth.lerp(pt, prevBodyHeadAngleDiff, bodyHeadAngleDiff);
            });
            variable.setFunction("smooth_head_x_rotation", () -> {
                if (controller != null && controller.getAvatar() instanceof Player player) {
                    float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                    return Mth.lerp(pt, prevHeadXRot, player.getXRot());
                }
                return 0F;
            });
        } else {
            LOGGER.warn("Failed to bind variable.get_movement_speed to Mocha");
        }
    }

    public void resetEffect() {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            pendingEffectPool.get(type).clear();
        }
    }

    // 在状态机tick之前调用，从Player读取最新数据
    public void serverTick(Player player) {
        if (this.jumpTimer > 0) this.jumpTimer--;
        if (this.slideCooldown > 0) this.slideCooldown--;
        if (this.breakFallTimer > 0) this.breakFallTimer--;
        if (this.dodgeTimer > 0) this.dodgeTimer--;
        if (this.dodgeCooldown > 0) this.dodgeCooldown--;
        if (this.vaultTimer > 0) this.vaultTimer--;
        if (this.jumpCooldown > 0) this.jumpCooldown--;
        if (this.breakFallReadyCount > 0) this.breakFallReadyCount--;
        if (this.wallJumpTimer > 0) this.wallJumpTimer--;
        if (this.swimPushTimer > 0) this.swimPushTimer--;
        if (this.wallKickCooldown > 0) this.wallKickCooldown--;
        boolean newHasJetBooster = checkBoosterEquipped(player);
        if (newHasJetBooster != this.hasJetBooster) {
            this.hasJetBooster = newHasJetBooster;
            applyBoosterAttributes(player, newHasJetBooster);
        }
        this.canMomentum = checkMomentum(this.hasJetBooster);
        if (player.fallDistance > 0) {
            this.lastFallDistance = player.fallDistance;
        }
    }

    public void clientTick(Player player) {
        if (this.slideCooldown == 1 && !SlideState.SLIDE.equals(this.currentAnimationName)){
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.3F, 1.5F);     // 穿皮革装备声
        }
        this.serverTick(player);
        // 保存上 tick 的值, 用于渲染帧 partialTick lerp (GeckoLib 风格)
        this.prevBodyHeadAngleDiff = this.bodyHeadAngleDiff;
        this.prevHeadXRot = player.getXRot();
        this.doubleClickUp = isDoubleClick(UP);
        this.doubleClickDown = isDoubleClick(DOWN);
        this.doubleClickLeft = isDoubleClick(LEFT);
        this.doubleClickRight = isDoubleClick(RIGHT);
        this.doubleClickJump = isDoubleClick(JUMP);
        this.doubleClickSprint = isDoubleClick(SPRINT);
        double moveSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double jumpStrength = player.getJumpBoostPower();
        this.jumpLimitSpeed = moveSpeed * (1 + jumpStrength) * 3;
        this.jumpAcceleration = jumpCooldown > 0 ? 0 : moveSpeed * (1 + jumpStrength) * 1.2;
        this.setWorldInputVec(player);
        this.detectWall(player);
        this.bodyHeadAngleDiff = Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot);
        this.tickCameraRoll();
        this.tickFovBonus();
        this.tickMomentumRoll(player);
        this.tickArmOffset();
    }

    public void clientTickRemote(Player player) {
        this.prevBodyHeadAngleDiff = this.bodyHeadAngleDiff;
        this.prevHeadXRot = player.getXRot();
        this.bodyHeadAngleDiff = Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot);
        if (this.jumpTimer > 0) this.jumpTimer--;
        if (this.slideCooldown > 0) this.slideCooldown--;
        if (this.breakFallTimer > 0) this.breakFallTimer--;
        if (this.dodgeTimer > 0) this.dodgeTimer--;
        if (this.dodgeCooldown > 0) this.dodgeCooldown--;
        if (this.vaultTimer > 0) this.vaultTimer--;
        if (this.jumpCooldown > 0) this.jumpCooldown--;
        if (this.breakFallReadyCount > 0) this.breakFallReadyCount--;
        if (this.wallJumpTimer > 0) this.wallJumpTimer--;
        if (this.swimPushTimer > 0) this.swimPushTimer--;
        if (this.wallKickCooldown > 0) this.wallKickCooldown--;
        remoteDetectWall(player);
    }

    public void tickArmOffset() {
        prevArmOffsetY = armOffsetY;
        prevArmRotX = armRotX;

        armOffsetY = Mth.lerp(0.2F, armOffsetY, targetArmOffsetY);
        armRotX = Mth.lerp(0.2F, armRotX, targetArmRotX);
    }

    public float getRenderArmOffsetY(float partialTick) {
        return Mth.lerp(partialTick, prevArmOffsetY, armOffsetY);
    }

    public float getRenderArmRotX(float partialTick) {
        return Mth.lerp(partialTick, prevArmRotX, armRotX);
    }

    public void setTargetArmTransform(float offsetY, float rotX) {
        this.targetArmOffsetY = offsetY;
        this.targetArmRotX = rotX;
    }

    // 设置一次随机数
    public void setLuckyNumber(Player player) {
        this.luckyNumber = player.getRandom().nextInt(100);
    }

    /**
     * 从速度方向变化率计算动画播放速度 (帧率无关, 纯标量, 无符号震荡).
     * <p>
     * 公式: Δθ = |atan2(Vy₂, H₂) - atan2(Vy₁, H₁)|  (每 tick 速度仰角变化)
     *       speed = 0.4 + clamp(Δθ° × 0.14, 0, 2.5)
     * <p>
     * 物理直觉: 跳跃顶点 Vy 过零时 Δθ 最大 → 动画最快通过水平位;
     *           匀速时 Δθ ≈ 0 → 动画慢放, 身体缓慢起伏.
     * <p>
     * 参数表: 5°/tick→1.1× | 10°/tick→1.8× | 15°/tick→2.9×(cap)
     *
     * @param curSpeed  当前 tick 速度向量
     * @param prevSpeed 上一 tick 速度向量
     * @return 动画播放速度倍率, 范围约 [0.4, 2.9]
     */
    public static float computeAnimSpeedFromVelocityAngle(Vec3 curSpeed, Vec3 prevSpeed) {
        float hCur = (float) curSpeed.horizontalDistance();
        float hPrev = (float) prevSpeed.horizontalDistance();
        double thetaCur = Math.atan2(curSpeed.y, Math.max(hCur, 0.001));
        double thetaPrev = Math.atan2(prevSpeed.y, Math.max(hPrev, 0.001));
        double deltaDeg = Math.toDegrees(Math.abs(thetaCur - thetaPrev));
        return (float) (0.4 + Math.clamp(deltaDeg * 0.14, 0.0, 2.5));
    }

    /**
     * 前馈+P 控制器: 让动画位置追踪速度仰角.
     * <p>
     * 核心思路: 动画速度 = 前馈(角度变化率) + P修正(位置误差)
     * 角度不变 → 前馈=0 → 动画暂停等待, 不会像旧版那样以基线1.0盲目前进然后来回振荡.
     * <p>
     * 例: velocity仰角 0°→ -45°(1 tick), range=90°, animLength=1s, kF=1.0
     * 前馈 = (45/90)×1×20×1 = 10 倍速追(1 tick就追上), P项再微调残差.
     *
     * @param velocity      当前速度向量
     * @param prevVelocity  上一 tick 速度向量 (用于计算角度变化率)
     * @param animTickSec   当前动画已播放时间 (秒), controller.tick / 20
     * @param animLengthSec 动画总时长 (秒)
     * @param angleTop      动画 t=0 对应的速度仰角 (°), 如 +60
     * @param angleBottom   动画 t=L 对应的速度仰角 (°), 如 -40
     * @param kP            比例增益, 2~4 推荐 (修正累积位置误差)
     * @param kF            前馈增益, 0.5~1.5 推荐 (1.0=角度变化全量映射到动画速度)
     * @return 动画速度倍率, [0.0, 5.0]
     */
    public static float computeAnimSpeedByAngleTracking(
            Vec3 velocity, Vec3 prevVelocity, float animTickSec, float animLengthSec,
            float angleTop, float angleBottom, float kP, float kF) {

        float h = (float) velocity.horizontalDistance();
        float thetaDeg = (float) Math.toDegrees(Math.atan2(velocity.y * 0.2F, Math.max(h, 0.001)));
        float thetaClamped = Mth.clamp(thetaDeg, angleBottom, angleTop);

        float hPrev = (float) prevVelocity.horizontalDistance();
        float thetaPrevDeg = (float) Math.toDegrees(Math.atan2(prevVelocity.y * 0.2F, Math.max(hPrev, 0.001)));
        float thetaPrevClamped = Mth.clamp(thetaPrevDeg, angleBottom, angleTop);

        float range = angleTop - angleBottom;
        if (range <= 1e-6f) return 1.0f;

        // 角度 → 目标动画位置
        float fraction = (angleTop - thetaClamped) / range;
        float targetPos = fraction * animLengthSec;

        // === 前馈: 角度变化率 → 期望动画速度 ===
        // 本 tick 角度变化量 (°), 正=向angleBottom移动
        float angleDelta = thetaPrevClamped - thetaClamped;
        // 映射为动画位置变化 / 秒
        float feedForward = (angleDelta / range) * animLengthSec * 20.0f * kF;

        // === P 修正: 消除累积位置误差 ===
        float error = targetPos - animTickSec;
        float pCorrection = kP * error;

        float speed = feedForward + pCorrection;
        return Mth.clamp(speed, 0.0f, 5.0f);
    }

    public void tickCameraRoll() {
        prevCameraRoll = currentCameraRoll;

        // 远离 0 时：稍慢，0.12
        // 回归 0 时：更快，0.25
        float smoothing = Math.abs(targetCameraRoll) > Math.abs(currentCameraRoll) ? 0.24F : 0.5F;

        float diff = targetCameraRoll - currentCameraRoll;
        currentCameraRoll += diff * smoothing;

        if (Math.abs(currentCameraRoll - targetCameraRoll) < 0.01F) {
            currentCameraRoll = targetCameraRoll;
        }
    }

    public void tickFovBonus() {
        prevFovBonus = currentFovBonus;
        float diff = targetFovBonus - currentFovBonus;
        currentFovBonus += diff * 0.15F;
    }

    public void tickMomentumRoll(Player player) {
        prevMomentumRoll = currentMomentumRoll;

        float targetRoll = 0F;

        if (momentumRollIntensity != 0F) {
            Vec3 velocity = player.getDeltaMovement();
            double horizontalSpeed = velocity.horizontalDistance();

            // 速度参数（每秒格数更直观）
            double speedPerSec = horizontalSpeed * 20;
            double minSpeed = 5.0;   // 最小速度阈值（每秒 4 格才开始有效果）
            double maxSpeed = 21.0;  // 最大速度（达到这个速度倾斜满）

            if (speedPerSec > minSpeed) {
                // 归一化方向
                Vec3 vDir = new Vec3(velocity.x, 0, velocity.z).normalize();
                float yaw = player.getYRot();
                Vec3 lookDir = new Vec3(
                        -Math.sin(Math.toRadians(yaw)),
                        0,
                        Math.cos(Math.toRadians(yaw))
                );

                // 叉积 y = sin(夹角)，范围 [-1, 1]
                double crossY = vDir.x * lookDir.z - vDir.z * lookDir.x;

                // 角度偏差阈值：sin 值小于这个不触发
                // sin(15°) ≈ 0.26，意味着夹角小于 15° 不触发
                double angleThreshold = 0.26;

                if (Math.abs(crossY) > angleThreshold) {
                    // 重新映射：把 [threshold, 1] 映射到 [0, 1]，避免突变
                    double sign = Math.signum(crossY);
                    double mappedCross = (Math.abs(crossY) - angleThreshold) / (1.0 - angleThreshold);
                    mappedCross = Math.min(1.0, mappedCross) * sign;

                    // 速度因子：[minSpeed, maxSpeed] 线性映射到 [0, 1]
                    float speedFactor = (float) Mth.clamp(
                            (speedPerSec - minSpeed) / (maxSpeed - minSpeed),
                            0.0, 1.0
                    );

                    targetRoll = (float) mappedCross * momentumRollIntensity * speedFactor;
                }
            }
        }

        // 平滑插值
        float smoothing = 0.2F;
        currentMomentumRoll = Mth.lerp(smoothing, currentMomentumRoll, targetRoll);

        if (Math.abs(currentMomentumRoll) < 0.01F && targetRoll == 0F) {
            currentMomentumRoll = 0F;
        }
    }

    public float getRenderMomentumRoll(float partialTick) {
        return Mth.lerp(partialTick, prevMomentumRoll, currentMomentumRoll);
    }

    public float getRenderCameraRoll(float partialTick) {
        return Mth.lerp(partialTick, prevCameraRoll, currentCameraRoll);
    }

    public float getRenderFovBonus(float partialTick) {
        return Mth.lerp(partialTick, prevFovBonus, currentFovBonus);
    }

    // 是否双击了某个键(两个true中至少隔一个false)
    public boolean isDoubleClick(int key) {
        int len = inputBuffer.length;
        int current = inputBufferIndex;

        // 当前必须是 true
        if (!inputBuffer[current].contains(key)) return false;

        boolean foundFirst = false;
        int idx = current;
        for (int i = 1; i < len; i++) {
            int lastIdx = idx;
            idx = (current + i) % len; // 从第一个开始找
            if (inputBuffer[idx].contains(key) && !foundFirst) {
                foundFirst = true;
                continue;
            }
            if (foundFirst) {
                for (int clickKey : inputBuffer[idx]) {
                    if (!inputBuffer[lastIdx].contains(clickKey)) {
                        // clickKey 是 这次 多出来的, 按下了其他键, 忽略判断和跳跃
                        if (clickKey != JUMP && clickKey != key) foundFirst = false;
                    }
                }
            }
        }
        return foundFirst;
    }

    // 判断在过去5个tick内(不包括当前)是否按下了指定按键
    public boolean wasKeyPressedRecently(int key, int offset) {
        int len = inputBuffer.length;
        int current = inputBufferIndex;

        for (int i = 1; i <= Math.min(offset, inputBuffer.length - 1); i++) {
            int idx = (current - i + len) % len;
            if (inputBuffer[idx].contains(key)) {
                return true;
            }
        };
        return false;
    }

    // 喷气助推器判断
    private boolean checkBoosterEquipped(Player player) {
        // 检查护腿槽
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(InitItems.JET_BOOSTER_ITEM.get())) {
            return true;
        }
        // 检查Curios腰饰槽（如果Curios存在）
        if (CuriosCompat.isLoaded() && CuriosHandler.hasJetBooster(player)) {
            return true;
        }
        return false;
    }

    private boolean checkMomentum(boolean hasJetBooster) {
        if (ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.get()) {
            return true;
        } else {
            return hasJetBooster;
        }
    }

    // 预计算墙面检测方向, 避免每 tick 重建
    private static final Vec3[] WALL_CARDINALS = {
            new Vec3(0, 0, -1),  // NORTH
            new Vec3(0, 0, 1),   // SOUTH
            new Vec3(1, 0, 0),   // EAST
            new Vec3(-1, 0, 0),  // WEST
    };
    private static final Vec3[] WALL_DIAGONALS = {
            new Vec3(1, 0, 1).normalize(),
            new Vec3(1, 0, -1).normalize(),
            new Vec3(-1, 0, 1).normalize(),
            new Vec3(-1, 0, -1).normalize(),
    };

    // 所有墙面法线方向 (cardinal 4 + diagonal 4), 用于网络同步编解码
    private static final Vec3[] ALL_WALL_NORMALS = {
            WALL_CARDINALS[0], WALL_CARDINALS[1], WALL_CARDINALS[2], WALL_CARDINALS[3],
            WALL_DIAGONALS[0], WALL_DIAGONALS[1], WALL_DIAGONALS[2], WALL_DIAGONALS[3],
    };

    public static int encodeWallNormal(Vec3 normal) {
        if (normal == null || normal.lengthSqr() < 0.001) return -1;
        for (int i = 0; i < ALL_WALL_NORMALS.length; i++) {
            if (ALL_WALL_NORMALS[i].distanceToSqr(normal) < 0.001) return i;
        }
        return -1;
    }

    public static Vec3 decodeWallNormal(int index) {
        if (index < 0 || index >= ALL_WALL_NORMALS.length) return Vec3.ZERO;
        return ALL_WALL_NORMALS[index];
    }

    private static class WallCandidate {
        Vec3 normal;
        AABB expanded;
        boolean hasFaceWall;
        boolean hasLedge;
        double ledgeOffsetY = 0; // 速降补偿找到ledge时的Y偏移量
        float lookAngle;        // 有符号，用于输出
        float inputAngle;       // 有符号，用于输出
        float lookAngleDiff;    // 无符号，用于排序
        float inputAngleDiff;   // 无符号，用于排序
    }

    // 客户端墙面数据判断
    public void detectWall(Player player) {
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        // sin/cos 已经归一化, 不需要再 normalize()
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        Vec3 inputVec = this.inputVec;
        boolean hasInput = inputVec.lengthSqr() > 0.001;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        // 先在 4 个垂直方向找候选，按优先级挑
        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

        // 垂直方向都没合适的，再看 4 个斜向
        if (best == null) {
            List<WallCandidate> diagCands = collectWallCandidates(
                    player, level, box, WALL_DIAGONALS, lookVec, inputVec, inputNorm, hasInput, reach);
            best = pickByPriority(diagCands, hasInput);
        }

        if (best == null) {
            this.hasFaceWall = false;
            this.hasLedge = false;
            this.ledgeOffsetY = 0;
            this.wallNormal = Vec3.ZERO;
            this.lookWallAngle = 360F;
            this.inputWallAngle = 360F;
            this.wallBlocks.clear();
            return;
        }

        // 应用结果
        wallBlocks.clear();
        BlockPos.betweenClosedStream(best.expanded).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                wallBlocks.add(pos.immutable());
            }
        });
        this.hasFaceWall = best.hasFaceWall;
        this.hasLedge = best.hasLedge;
        this.ledgeOffsetY = best.ledgeOffsetY;
        this.wallNormal = best.normal;
        this.lookWallAngle = best.lookAngle;
        this.inputWallAngle = hasInput ? best.inputAngle : 360F;
    }

    private List<WallCandidate> collectWallCandidates(
            Player player, Level level, AABB box, Vec3[] normals,
            Vec3 lookVec, Vec3 inputVec, Vec3 inputNorm, boolean hasInput, double reach) {
        List<WallCandidate> result = new ArrayList<>();

        double eyeY = player.getEyeY();
        double topY = box.maxY;
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;

        for (Vec3 normal : normals) {
            AABB expanded = box.expandTowards(normal.x * reach, 0, normal.z * reach);
            if (level.noCollision(player, expanded)) continue;

            WallCandidate c = new WallCandidate();
            c.normal = normal;
            c.expanded = expanded;

            // 视角角度
            double lookDot = lookVec.x * normal.x + lookVec.z * normal.z;
            double lookCross = lookVec.x * normal.z - lookVec.z * normal.x;
            c.lookAngle = (float) Math.toDegrees(Math.atan2(lookCross, lookDot));
            c.lookAngleDiff = (float) Math.toDegrees(
                    Math.acos(Math.min(1.0, Math.max(-1.0, lookDot))));

            // 输入角度
            if (hasInput) {
                double inputDot = inputVec.x * normal.x + inputVec.z * normal.z;
                double inputCross = inputVec.x * normal.z - inputVec.z * normal.x;
                c.inputAngle = (float) Math.toDegrees(Math.atan2(inputCross, inputDot));
                double normDot = inputNorm.x * normal.x + inputNorm.z * normal.z;
                c.inputAngleDiff = (float) Math.toDegrees(
                        Math.acos(Math.min(1.0, Math.max(-1.0, normDot))));
            } else {
                c.inputAngle = 360F;
                c.inputAngleDiff = 360F;
            }

            // 边缘检测
            AABB ledgeBox = new AABB(
                    box.minX + normal.x * reach, eyeY, box.minZ + normal.z * reach,
                    box.maxX + normal.x * reach, topY, box.maxZ + normal.z * reach
            );
            AABB chinBox = new AABB(
                    box.minX + normal.x * reach, chinY, box.minZ + normal.z * reach,
                    box.maxX + normal.x * reach, eyeY, box.maxZ + normal.z * reach
            );
            c.hasFaceWall = !level.noCollision(player, chinBox);
            c.hasLedge = level.noCollision(player, ledgeBox) && c.hasFaceWall;

            // 速降补偿: 如果当前位置没有ledge但下落速度快, 沿下落路径向上回溯
            if (!c.hasLedge && c.hasFaceWall && player.getDeltaMovement().y < -0.4) {
                double fallDist = Math.abs(player.getDeltaMovement().y);
                for (double yOff = 0.2; yOff <= fallDist + 0.5; yOff += 0.2) {
                    AABB sweepLedgeBox = new AABB(
                            box.minX + normal.x * reach, eyeY + yOff, box.minZ + normal.z * reach,
                            box.maxX + normal.x * reach, topY + yOff, box.maxZ + normal.z * reach
                    );
                    AABB sweepChinBox = new AABB(
                            box.minX + normal.x * reach, chinY + yOff, box.minZ + normal.z * reach,
                            box.maxX + normal.x * reach, eyeY + yOff, box.maxZ + normal.z * reach
                    );
                    if (level.noCollision(player, sweepLedgeBox) && !level.noCollision(player, sweepChinBox)) {
                        c.hasLedge = true;
                        c.ledgeOffsetY = yOff;
                        break;
                    }
                }
            }

            result.add(c);
        }
        return result;
    }

    private WallCandidate pickByPriority(List<WallCandidate> candidates, boolean hasInput) {
        if (candidates.isEmpty()) return null;

        // 优先选有边缘的；如果没有，回退到所有候选
        List<WallCandidate> ledged = new ArrayList<>();
        for (WallCandidate c : candidates) {
            if (c.hasLedge) ledged.add(c);
        }
        List<WallCandidate> pool = !ledged.isEmpty() ? ledged : candidates;

        // 池内：有输入按输入选最近，没输入按视角选最近
        WallCandidate best = null;
        float bestDiff = Float.MAX_VALUE;
        for (WallCandidate c : pool) {
            float diff = hasInput ? c.inputAngleDiff : c.lookAngleDiff;
            if (diff < bestDiff) {
                bestDiff = diff;
                best = c;
            }
        }
        return best;
    }


    public void setWorldInputVec(Player player) {
        Vec2 moveVec = ((LocalPlayer) player).input.getMoveVector();
        float forward = moveVec.y;
        float strafe = moveVec.x;

        float yaw = player.getYRot();
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double x = strafe * cos - forward * sin;
        double z = forward * cos + strafe * sin;

        this.inputVec = new Vec3(x, 0, z);
    }

    // 非客户端本地玩家墙面数据判断
    public void remoteDetectWall(Player player) {
        // 有网络同步的墙面数据 → 直接使用, 跳过 collectWallCandidates 扫描
        // 没有同步数据 → 扫描墙面 (用速度方向替代键盘输入)
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        // 远程玩家没有键盘输入, 用速度方向替代
        Vec3 velocity = this.speed;
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        // 垂直方向：先 ledged 后普通
        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

        // 斜向兜底
        if (best == null) {
            List<WallCandidate> diagCands = collectWallCandidates(
                    player, level, box, WALL_DIAGONALS, lookVec, inputVec, inputNorm, hasInput, reach);
            best = pickByPriority(diagCands, hasInput);
        }

        if (best == null) {
            if (transitionWallData >= 0) {
                applySyncedWallData(player);
                return;
            }
            this.setLookWallAngle(360F);
            this.setWallNormal(Vec3.ZERO);
            this.setInputWallAngle(360F);
            this.setHasFaceWall(false);
            this.setHasLedge(false);
            this.setLedgeOffsetY(0);
            return;
        }

        this.setWallNormal(best.normal);
        this.setLookWallAngle(best.lookAngle);
        this.setInputWallAngle(hasInput ? best.inputAngle : 360F);
        this.setHasFaceWall(best.hasFaceWall);
        this.setHasLedge(best.hasLedge);
        this.setLedgeOffsetY(best.ledgeOffsetY);
    }

    // 使用网络同步的墙面数据计算 WallCandidate 的各项内容
    private void applySyncedWallData(Player player) {
        int wallIndex = this.transitionWallData & 0x7;

        Vec3 wallNormal = decodeWallNormal(wallIndex);
        if (Vec3.ZERO.equals(wallNormal)) {
            this.setWallNormal(Vec3.ZERO);
            this.setLookWallAngle(360F);
            this.setInputWallAngle(360F);
            this.setHasFaceWall(false);
            this.setHasLedge(false);
            this.setLedgeOffsetY(0);
            return;
        }

        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        // 计算视线角度
        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        double lookDot = lookVec.x * wallNormal.x + lookVec.z * wallNormal.z;
        double lookCross = lookVec.x * wallNormal.z - lookVec.z * wallNormal.x;
        float lookAngle = (float) Math.toDegrees(Math.atan2(lookCross, lookDot));

        // 用速度方向替代输入方向计算 inputAngle
        Vec3 velocity = player.getDeltaMovement();
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        float inputAngle = 360F;
        if (hasInput) {
            double inputDot = inputVec.x * wallNormal.x + inputVec.z * wallNormal.z;
            double inputCross = inputVec.x * wallNormal.z - inputVec.z * wallNormal.x;
            inputAngle = (float) Math.toDegrees(Math.atan2(inputCross, inputDot));
        }

        // hasFaceWall / hasLedge 检测 (仅检测同步来的墙面方向)
        AABB expanded = box.expandTowards(wallNormal.x * reach, 0, wallNormal.z * reach);
        double eyeY = player.getEyeY();
        double topY = box.maxY;
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;
        AABB chinBox = new AABB(
                box.minX + wallNormal.x * reach, chinY, box.minZ + wallNormal.z * reach,
                box.maxX + wallNormal.x * reach, eyeY, box.maxZ + wallNormal.z * reach
        );
        AABB ledgeBox = new AABB(
                box.minX + wallNormal.x * reach, eyeY, box.minZ + wallNormal.z * reach,
                box.maxX + wallNormal.x * reach, topY, box.maxZ + wallNormal.z * reach
        );
        boolean hasFaceWall = !level.noCollision(player, chinBox);
        boolean hasLedge = level.noCollision(player, ledgeBox) && hasFaceWall;

        // 写入 context
        this.setWallNormal(wallNormal);
        this.setLookWallAngle(lookAngle);
        this.setInputWallAngle(hasInput ? inputAngle : 360F);
        this.setHasFaceWall(hasFaceWall);
        this.setHasLedge(hasLedge);
        this.setLedgeOffsetY(0);

        // 填充 wallBlocks (用于音效播放)
        this.wallBlocks.clear();
        BlockPos.betweenClosedStream(expanded).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) wallBlocks.add(pos.immutable());
        });
    }

    public void playWallSound(Player player, Function<SoundType, SoundEvent> kind, float volumeMultiplier, float pitchMultiplier) {
        if (wallBlocks.isEmpty()) return;

        Level level = player.level();
        BlockPos pos = wallBlocks.get(level.getRandom().nextInt(wallBlocks.size()));
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        SoundType soundType = state.getSoundType(level, pos, player);
        player.playSound(
                kind.apply(soundType),
                soundType.getVolume() * volumeMultiplier,
                soundType.getPitch() * pitchMultiplier
        );
    }

    // 向指定效果添加永久buff
    public void addPermanentEffect(MomentumEffectType type, MomentumEffect effect) {
        effect.setDuration(-1);
        effect.setElapsedDuration(0);
        this.pendingEffectPool.get(type).add(effect);
    }

    // 向指定效果添加时效buff
    public void addEffect(MomentumEffectType type, MomentumEffect effect, int duration) {
        effect.setDuration(duration);
        effect.setElapsedDuration(0);
        this.pendingEffectPool.get(type).add(effect);
    }

    // 为指定效果去除buff
    public void removeEffect(MomentumEffectType type, MomentumEffect effect) {
        effect.setElapsedDuration(0);
        this.pendingEffectPool.get(type).remove(effect);
    }

    public void setJumpAnimationSpeed(float jumpAnimationSpeed) {
        if (jumpAnimationSpeed > MAX_SAFE_SPEED){
            jumpAnimationSpeed = MAX_SAFE_SPEED;
        }
        this.jumpAnimationSpeed = jumpAnimationSpeed;
    }
}