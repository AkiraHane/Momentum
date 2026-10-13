package com.akirahane.momentum.core.context;

import com.akirahane.momentum.client.MomentumClient;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.client.config.ClientConfig;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.init.InitAttachments;
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
    // ==================== 通用变量 ====================

    // ---------- 日志与调试 ----------
    protected static final Logger LOGGER = LogUtils.getLogger();
    private int debugFrameCalls = 0;
    private int debugTickCounter = 0;

    // ---------- 输入 ----------
    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, JUMP = 4, SPRINT = 5, SHIFT = 6, SHIFT_HOLD = 7;
    private int inputBufferIndex = 0;
    private final int inputBufferSize = 10;
    @SuppressWarnings("unchecked")
    private final HashSet<Integer>[] inputBuffer = new HashSet[inputBufferSize];

    private boolean doubleClickUp = false;
    private boolean doubleClickDown = false;
    private boolean doubleClickLeft = false;
    private boolean doubleClickRight = false;
    private boolean doubleClickJump = false;
    private boolean doubleClickSprint = false;

    private boolean noMoveInput = false;
    private boolean noJump = false;
    // 世界坐标下的移动输入。
    private Vec3 inputVec = Vec3.ZERO;

    // ---------- 移动数据 ----------
    // 相邻 tick 的真实位移（格/tick）。
    private Vec3 speed = Vec3.ZERO;
    private Vec3 oldSpeed = Vec3.ZERO;
    // 上一 tick 的 DeltaMovement。
    private Vec3 oldDeltaMovement = Vec3.ZERO;
    // 水平跳跃加速上限与加速量（格/tick）。
    private double jumpLimitSpeed = 0;
    private double jumpAcceleration = 0;

    // ---------- 墙面检测 ----------
    private boolean hasFaceWall = false;
    private boolean hasLedge = false;
    // 指向墙面的法向量。
    Vec3 wallNormal = Vec3.ZERO;
    // 有符号夹角（度）；360 表示无墙面或无输入方向。
    float lookWallAngle = 360F;
    float inputWallAngle = 360F;
    // 快速下落时的抓边高度补偿。
    private double ledgeOffsetY = 0;

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

    // 方向顺序对应网络编号，调整顺序会改变协议。
    private static final Vec3[] ALL_WALL_NORMALS = {
            WALL_CARDINALS[0], WALL_CARDINALS[1], WALL_CARDINALS[2], WALL_CARDINALS[3],
            WALL_DIAGONALS[0], WALL_DIAGONALS[1], WALL_DIAGONALS[2], WALL_DIAGONALS[3],
    };

    // ---------- 装备与机动条件 ----------
    private boolean hasJetBooster = false;
    private boolean canMomentum = true;

    // ---------- 状态与同步 ----------
    private StateType previousStateType = StateType.ORIGINAL;
    // 含义随动作而变；-1 表示无数据。
    private int transitionExtraData = -1;
    // bit 0-2: wallNormal 索引；bit 3: inputWallAngle 左右标志；-1=无墙面数据。
    private byte transitionWallData = -1;

    // ---------- 共用计时与冷却（tick） ----------
    // 闪避与水中推进共用。
    private int dodgeCooldown = 0;
    // 蹬墙跳与蓄力跳共用。
    private int wallJumpTimer = 0;
    // 翻越与翻入共用。
    private int vaultTimer = 0;

    // ---------- 运动效果 ----------
    private final Map<MomentumEffectType, Set<MomentumEffect>> pendingEffectPool = new HashMap<>();

    // 机动模式总阻力。
    public static MomentumEffect DEFAULT_FRICTION = new MomentumEffect(
            new Vec3(0.9, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );

    // 多个空中、贴墙动作共用的水平加速限速。
    public static MomentumEffect AIR_LIMIT_ACCELERATION = new MomentumEffect(
            new Vec3(0.3, Float.MAX_VALUE, 0.3),
            Vec3.ZERO,
            COMPOSE,
            -1
    );

    // ---------- 动画 ----------
    MomentumAnimationController controller;
    MochaEngine<AnimationController> mocha;
    private String currentAnimationName = null;
    // 下一次跳跃动画是否使用左脚。
    private boolean leftFootJump = true;
    private float jumpAnimationSpeed = 1;
    // 相邻 tick 的头身角度（度），用于动画插值。
    private float bodyHeadAngleDiff = 0F;
    private float prevBodyHeadAngleDiff = 0F;
    private float prevHeadXRot = 0F;

    // ---------- 相机 ----------
    // 相机 roll（度）。
    private float targetCameraRoll = 0F;
    private float currentCameraRoll = 0F;
    private float prevCameraRoll = 0F;

    // FOV 增量（度）。
    private float targetFovBonus = 0F;
    private float currentFovBonus = 0F;
    private float prevFovBonus = 0F;

    private float momentumRollIntensity = 0F;  // 当前动作的最大倾斜角度
    private float currentMomentumRoll = 0F;
    private float prevMomentumRoll = 0F;

    // ---------- 第一人称手臂 ----------
    private float armOffsetY = 0F;
    private float prevArmOffsetY = 0F;
    private float targetArmOffsetY = 0F;

    // 手臂 X 旋转角度（度）。
    private float armRotX = 0F;
    private float prevArmRotX = 0F;
    private float targetArmRotX = 0F;

    // ---------- 声音 ----------
    // 墙面音效播放间隔的剩余计数。
    private float needSoundTick = 0;
    // 用于选择墙面音效的方块列表。
    private final List<BlockPos> wallBlocks = new ArrayList<>();
    public static final Function<SoundType, SoundEvent> STEP = SoundType::getStepSound,
            HIT = SoundType::getHitSound,
            BREAK = SoundType::getBreakSound,
            PLACE = SoundType::getPlaceSound,
            FALL = SoundType::getFallSound;

    // ---------- 用途待确认 ----------
    private int jumpTimer = 0;
    private int jumpCooldown = 0;
    private float wallFriction = 0.6f;
    private int airborneTimer = 0;
    private double lastFallDistance = 0;
    private final int dodgeInvincible = 10;

    // ==================== 功能变量 ====================

    // ---------- 滑铲 ----------
    private int slideCooldown = 0;
    private boolean slideJumpCooldownJustFinished = false;
    // 台阶高度变化与坡向单位向量。
    private double blockStep = 0;
    private Vec3 slopeUnitVector = Vec3.ZERO;

    public MomentumEffect SLIDE_FRICTION = new MomentumEffect(
            new Vec3(0.1, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );

    public MomentumEffect SLIDE_BLOCK_FRICTION = new MomentumEffect(
            Vec3.ZERO,
            Vec3.ZERO,
            MULTIPLIER,
            0
    );

    public MomentumEffect SLIDE_ACCELERATION = new MomentumEffect(
            Vec3.ZERO,
            Vec3.ZERO,
            COMPOSE,
            0
    );

    // ---------- 受身 ----------
    private boolean toBreakFallState = false;
    private int breakFallTimer = 0;
    // 受身准备剩余 tick；-1 表示未准备。
    private int breakFallReadyCount = -1;
    // 受身准备提示的彩蛋随机数。
    private int luckyNumber = 0;

    public MomentumEffect BREAK_FALL_FRICTION = new MomentumEffect(
            new Vec3(0.5, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            0
    );

    // ---------- 闪避 ----------
    private int dodgeTimer = 0;

    public MomentumEffect DODGE_BLOCK_FRICTION = new MomentumEffect(
            new Vec3(0, 0, 0),
            Vec3.ZERO,
            MULTIPLIER,
            0
    );

    // ---------- 墙跑 ----------
    // 进入墙跑时的墙面法向量。
    Vec3 runWallNormal = Vec3.ZERO;
    // 已应用的重力倍率，用于避免重复更新属性。
    private float gravityModify = 0;
    // 蹬墙跳后再次进入墙跑的速度判定宽限（tick）。
    private int wallRunReentryGraceTicks = 0;

    // ---------- 蹬墙跳 ----------
    private int wallKickCooldown = 0;

    // ---------- 挂墙 ----------
    public static MomentumEffect WALL_FRICTION = new MomentumEffect(
            new Vec3(0.4, 0, 0),
            Vec3.ZERO,
            COMPOSE,
            -1
    );

    // ---------- 空中移动 ----------
    public MomentumEffect AIR_ACCELERATION = new MomentumEffect(
            new Vec3(1, 1, 1),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );

    // ---------- 水中推进 ----------
    private int swimPushTimer = 0;

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
            math.setFunction("clamp", Mth::clamp);
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
            variable.setFunction("dolphin_tilt", () -> {
                float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                Vec3 speed = getSpeed();
                Vec3 old = oldSpeed;
                float h = Mth.lerp(pt, (float) old.horizontalDistance(), (float) speed.horizontalDistance());
                float vy = Mth.lerp(pt, (float) old.y, (float) speed.y);
                float thetaDeg = (float) Math.toDegrees(Math.atan2(vy, Math.max(h, 0.001)));
                return -thetaDeg;
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

    // 本地与服务端共用的计时器、装备更新。
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
        if (this.wallRunReentryGraceTicks > 0) this.wallRunReentryGraceTicks--;
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
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.3F, 1.5F);
        }
        this.serverTick(player);
        // 保存上一 tick 姿态，供渲染插值使用。
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
        // 越接近速度上限，跳跃加速收益越小。
        double baseJumpAccel = moveSpeed * (1 + jumpStrength) * 1.2;
        double jumpRatio = this.speed.horizontalDistance() / Math.max(this.jumpLimitSpeed, 0.001);
        this.jumpAcceleration = baseJumpAccel * Math.max(0.0, 1.0 - jumpRatio * jumpRatio);
        this.setWorldInputVec(player);
        this.detectWall(player);
        this.bodyHeadAngleDiff = Mth.wrapDegrees(player.getYHeadRot() - player.yBodyRot);
        this.tickCameraRoll();
        this.computeTargetFovBonus(player);
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
        if (this.wallRunReentryGraceTicks > 0) this.wallRunReentryGraceTicks--;
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

    public void setLuckyNumber(Player player) {
        this.luckyNumber = player.getRandom().nextInt(100);
    }

    /**
     * 根据相邻 tick 的速度仰角变化计算动画播放速度。
     * @return 播放速度倍率，范围 [0.4, 2.9]
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
     * 用速度方向变化和位置误差，使动画进度跟随速度仰角。
     * 时间参数以秒计，角度以度计；angleTop/angleBottom 对应动画起点/终点。
     * @param kP 动画位置误差增益
     * @param kF 速度仰角变化率增益
     * @return 播放速度倍率，范围 [0, 5]
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

        float fraction = (angleTop - thetaClamped) / range;
        float targetPos = fraction * animLengthSec;

        // 正值表示向动画终点移动。
        float angleDelta = thetaPrevClamped - thetaClamped;
        float feedForward = (angleDelta / range) * animLengthSec * 20.0f * kF;

        float error = targetPos - animTickSec;
        float pCorrection = kP * error;

        float speed = feedForward + pCorrection;
        return Mth.clamp(speed, 0.0f, 5.0f);
    }

    public void tickCameraRoll() {
        prevCameraRoll = currentCameraRoll;

        // 回正速度快于倾斜速度。
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


    private void computeTargetFovBonus(Player player) {
        MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
        if (sm.getCurrentState().getStateType() == StateType.ORIGINAL) {
            this.targetFovBonus = 0F;
            return;
        }
        if (!ClientConfig.ENABLE_CAMERA_OFFSET.get()) {
            this.targetFovBonus = 0F;
            return;
        }
        double speedPerSec = getSpeed().horizontalDistance() * 20;
        double minSpeed = 5.0;
        double maxSpeed = 21.0;
        float speedFactor = (float) Mth.clamp((speedPerSec - minSpeed) / (maxSpeed - minSpeed), 0.0, 1.0);
        // 遵循原版“视场角效果”选项。
        float fovEffectScale = Minecraft.getInstance().options.fovEffectScale().get().floatValue();
        this.targetFovBonus = speedFactor * (float) ClientConfig.FOV_BONUS_MAX.get().doubleValue() * fovEffectScale;
    }

    public void tickMomentumRoll(Player player) {
        prevMomentumRoll = currentMomentumRoll;

        float targetRoll = 0F;

        if (momentumRollIntensity != 0F) {
            Vec3 velocity = player.getDeltaMovement();
            double horizontalSpeed = velocity.horizontalDistance();

            double speedPerSec = horizontalSpeed * 20;
            double minSpeed = 5.0;
            double maxSpeed = 21.0;

            if (speedPerSec > minSpeed) {
                Vec3 vDir = new Vec3(velocity.x, 0, velocity.z).normalize();
                float yaw = player.getYRot();
                Vec3 lookDir = new Vec3(
                        -Math.sin(Math.toRadians(yaw)),
                        0,
                        Math.cos(Math.toRadians(yaw))
                );

                // 叉积 y = sin(夹角)，范围 [-1, 1]
                double crossY = vDir.x * lookDir.z - vDir.z * lookDir.x;

                // sin(15°)，忽略小角度偏差。
                double angleThreshold = 0.26;

                if (Math.abs(crossY) > angleThreshold) {
                    // 从阈值起连续增加倾斜，避免突变。
                    double sign = Math.signum(crossY);
                    double mappedCross = (Math.abs(crossY) - angleThreshold) / (1.0 - angleThreshold);
                    mappedCross = Math.min(1.0, mappedCross) * sign;

                    float speedFactor = (float) Mth.clamp(
                            (speedPerSec - minSpeed) / (maxSpeed - minSpeed),
                            0.0, 1.0
                    );

                    targetRoll = (float) mappedCross * momentumRollIntensity * speedFactor;
                }
            }
        }

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

    // 查询当前按键是否在缓冲期内再次按下。
    public boolean isDoubleClick(int key) {
        int len = inputBuffer.length;
        int current = inputBufferIndex;

        if (!inputBuffer[current].contains(key)) return false;

        boolean foundFirst = false;
        int idx = current;
        for (int i = 1; i < len; i++) {
            int lastIdx = idx;
            idx = (current + i) % len;
            if (inputBuffer[idx].contains(key) && !foundFirst) {
                foundFirst = true;
                continue;
            }
            if (foundFirst) {
                for (int clickKey : inputBuffer[idx]) {
                    if (!inputBuffer[lastIdx].contains(clickKey)) {
                        // 跳跃以外的其他新按键会打断双击匹配。
                        if (clickKey != JUMP && clickKey != key) foundFirst = false;
                    }
                }
            }
        }
        return foundFirst;
    }

    // 查询前 offset 个 tick 的按键记录，不含当前 tick。
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

    private boolean checkBoosterEquipped(Player player) {
        if (player.getItemBySlot(EquipmentSlot.LEGS).is(InitItems.JET_BOOSTER_ITEM.get())) {
            return true;
        }
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

    // 墙面检测候选。
    private static class WallCandidate {
        Vec3 normal;
        // 用于收集墙面音效方块。
        AABB expanded;
        boolean hasFaceWall;
        boolean hasLedge;
        double ledgeOffsetY = 0; // 抓边高度补偿
        float lookAngle;        // 有符号，用于输出
        float inputAngle;       // 有符号，用于输出
        float lookAngleDiff;    // 无符号，用于排序
        float inputAngleDiff;   // 无符号，用于排序
    }

    public void detectWall(Player player) {
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        Vec3 inputVec = this.inputVec;
        boolean hasInput = inputVec.lengthSqr() > 0.001;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        // 优先四个正方向，再尝试斜向。
        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

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

            // 快速下落可能越过边缘，沿下落路径回溯检测。
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

        // 优先可抓取边缘。
        List<WallCandidate> ledged = new ArrayList<>();
        for (WallCandidate c : candidates) {
            if (c.hasLedge) ledged.add(c);
        }
        List<WallCandidate> pool = !ledged.isEmpty() ? ledged : candidates;

        // 有输入时按输入方向选墙，否则按视线方向。
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

    public void remoteDetectWall(Player player) {
        // 扫描墙面，无候选时回退到同步方向。
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        // 以位移方向代替远程玩家的输入。
        Vec3 velocity = this.speed;
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

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

        // 以速度方向估算输入夹角。
        Vec3 velocity = player.getDeltaMovement();
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        float inputAngle = 360F;
        if (hasInput) {
            double inputDot = inputVec.x * wallNormal.x + inputVec.z * wallNormal.z;
            double inputCross = inputVec.x * wallNormal.z - inputVec.z * wallNormal.x;
            inputAngle = (float) Math.toDegrees(Math.atan2(inputCross, inputDot));
        }

        // 只检测同步方向上的墙壁和边缘。
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

        this.setWallNormal(wallNormal);
        this.setLookWallAngle(lookAngle);
        this.setInputWallAngle(hasInput ? inputAngle : 360F);
        this.setHasFaceWall(hasFaceWall);
        this.setHasLedge(hasLedge);
        this.setLedgeOffsetY(0);

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

    // 永久效果需主动移除。
    public void addPermanentEffect(MomentumEffectType type, MomentumEffect effect) {
        effect.setDuration(-1);
        effect.setElapsedDuration(0);
        this.pendingEffectPool.get(type).add(effect);
    }

    public void addEffect(MomentumEffectType type, MomentumEffect effect, int duration) {
        effect.setDuration(duration);
        effect.setElapsedDuration(0);
        this.pendingEffectPool.get(type).add(effect);
    }

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
