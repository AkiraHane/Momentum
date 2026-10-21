package com.akirahane.momentum.core.context;

import com.akirahane.momentum.client.utils.AnimationUtils;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.StateType;
import com.zigythebird.playeranimcore.animation.AnimationController;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import team.unnamed.mocha.MochaEngine;
import java.util.*;

import static com.akirahane.momentum.client.animation.MomentumAnimationController.MAX_SAFE_SPEED;
import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.*;

@Getter
@Setter
public class PlayerMomentumContext {
    // ==================== 通用变量 ====================

    // ---------- 调试 ----------
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
    // 本 tick 检测到的全部有效墙面，已排除冲突斜向并按通用优先级排序，供各动作筛选。
    private List<WallContact> wallCandidates = List.of();
    // 当前动作已绑定的墙面；检测时刷新同一方向的数据，动作退出时清除。
    private WallContact activeWall;
    // 当前墙面所属的动作，用于区分“原动作保持墙面”和“新动作重新选墙”。
    private StateType activeWallState;
    // 当前候选动作试选的墙面。每次判断下一个动作前清除，动作确定后才提交为 activeWall。
    private WallContact pendingWall;

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
    // 临近挂墙边缘时，水平移动加速度减少；原速度由挂墙摩擦逐渐衰减。
    public static final MomentumEffect WALL_HANG_EDGE_ACCELERATION = new MomentumEffect(
            new Vec3(0.1, 1, 0.1),
            Vec3.ZERO,
            MULTIPLIER,
            -1
    );
    // 边缘减速期间额外增加的摩擦，与普通挂墙摩擦叠加。
    public static final MomentumEffect WALL_HANG_EDGE_FRICTION = new MomentumEffect(
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

    public PlayerMomentumContext(Player player) {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            pendingEffectPool.put(type, new HashSet<>());
        }
        for (int i = 0; i < inputBufferSize; i++) {
            inputBuffer[i] = new HashSet<>();
        }
        if (player.level().isClientSide()) {
            AnimationUtils.initialize(player, this);
        }
    }

    public void resetEffect() {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            pendingEffectPool.get(type).clear();
        }
    }

    public void tickTimers() {
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
    }

    public Vec3 getWallNormal() {
        return activeWall == null ? Vec3.ZERO : activeWall.normal();
    }

    public boolean isHasFaceWall() {
        return activeWall != null && activeWall.hasFaceWall();
    }

    public boolean isHasLedge() {
        return activeWall != null && activeWall.hasLedge();
    }

    public float getLookWallAngle() {
        return activeWall == null ? 360F : activeWall.lookAngle();
    }

    public float getInputWallAngle() {
        return activeWall == null ? 360F : activeWall.inputAngle();
    }

    public double getLedgeOffsetY() {
        return activeWall == null ? 0 : activeWall.ledgeOffsetY();
    }

    public List<BlockPos> getWallBlocks() {
        return activeWall == null ? List.of() : activeWall.blocks();
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

    public float getRenderMomentumRoll(float partialTick) {
        return Mth.lerp(partialTick, prevMomentumRoll, currentMomentumRoll);
    }

    public float getRenderCameraRoll(float partialTick) {
        return Mth.lerp(partialTick, prevCameraRoll, currentCameraRoll);
    }

    public float getRenderFovBonus(float partialTick) {
        return Mth.lerp(partialTick, prevFovBonus, currentFovBonus);
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
