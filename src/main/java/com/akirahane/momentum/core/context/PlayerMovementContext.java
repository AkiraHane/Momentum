package com.akirahane.momentum.core.context;

import com.akirahane.momentum.client.MomentumClient;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationController;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
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

import static com.akirahane.momentum.core.MomentumUtils.HORIZONTALS;
import static com.akirahane.momentum.core.MomentumUtils.applyBoosterAttributes;
import static com.akirahane.momentum.core.effect.MomentumEffect.EffectType.*;

@Getter
@Setter
public class PlayerMovementContext {
    // 静态量
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 键位
    public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, JUMP = 4;
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
    // 眼睛到下巴的前方是否有可抓取墙壁
    private boolean hasFaceWall = false;
    // 是否双击UP DOWN LEFT RIGHT JUMP
    private boolean doubleClickUp = false;
    private boolean doubleClickDown = false;
    private boolean doubleClickLeft = false;
    private boolean doubleClickRight = false;
    private boolean doubleClickJump = false;
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
    // 交互的墙面方向
    Direction wallDirection = null;
    // 向墙的法向量
    Vec3 wallNormal = Vec3.ZERO;
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
    // 受身计时器
    private int breakFallTimer = 0;
    // 闪避计时器
    private int dodgeTimer = 0;
    // 翻越计时器
    private int vaultTimer = 0;
    // 受身计数器
    private int breakFallReadyCount = -1;

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
            math.setFunction("min_angle_180", (a, b) -> {
                double diff = b - a;
                // 用取模运算将差值初步约束到 (-360°, 360°)
                diff = diff % 360.0;
                // 进一步规范化到目标区间
                if (diff < -180.0) {
                    diff += 360.0;
                } else if (diff > 180.0) {
                    diff -= 360.0;
                }
                // 处理边界：当结果为 -180° 时，可统一返回 180°（因为几何意义等价）
                if (diff == -180.0) {
                    diff = 180.0;
                }
                return (float) diff;
            });
            // clamp
            math.setFunction("clamp", Mth::clamp);
            // min
            math.setFunction("min", Math::min);
        } else {
            LOGGER.warn("Failed to bind math.min_angle_180 to Mocha");
        }
        value = this.mocha.scope().get("variable");
        if (value instanceof ObjectValue variable) {
            variable.setFunction("get_movement_speed", () -> (float) this.getSpeed().horizontalDistance());
            // y speed
            variable.setFunction("get_movement_y_speed", () -> (float) this.getSpeed().y());
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
        if (this.vaultTimer > 0) this.vaultTimer--;
        if (this.jumpCooldown > 0) this.jumpCooldown--;
        if (this.breakFallReadyCount > 0) this.breakFallReadyCount--;
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
        this.serverTick(player);
        this.doubleClickUp = isDoubleClick(UP);
        this.doubleClickDown = isDoubleClick(DOWN);
        this.doubleClickLeft = isDoubleClick(LEFT);
        this.doubleClickRight = isDoubleClick(RIGHT);
        this.doubleClickJump = isDoubleClick(JUMP);
        double moveSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double jumpStrength = player.getJumpBoostPower();
        this.jumpLimitSpeed = moveSpeed * (1 + jumpStrength) * 3;
        this.jumpAcceleration = jumpCooldown > 0 ? 0.2 : moveSpeed * (1 + jumpStrength) * 1.2;
        this.setWorldInputVec(player);
        this.detectWall(player);
    }

    public void clientTickRemote(Player player) {
        remoteDetectWall(player);
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

    // 喷气助推器判断
    private boolean checkBoosterEquipped(Player player) {
        // 检查Curios腰饰槽（如果Curios存在）
        if (!CuriosCompat.isLoaded()) {
            return false;
        }
        return CuriosHandler.hasJetBooster(player);
    }

    private boolean checkMomentum(boolean hasJetBooster) {
        if (ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.get()) {
            return true;
        } else {
            return hasJetBooster;
        }
    }

    // 客户端墙面数据判断
    public void detectWall(Player player) {
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.2;

        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        ).normalize();

        Vec3 inputVec = this.inputVec;
        boolean hasInput = inputVec.lengthSqr() > 0.001;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : Vec3.ZERO;

        Direction bestDir = null;
        float bestLookAngle = 360F;
        float bestInputAngle = 360F;

        for (Direction dir : HORIZONTALS) {
            AABB expanded = box.expandTowards(
                    dir.getStepX() * reach,
                    0,
                    dir.getStepZ() * reach
            );

            if (!level.noCollision(player, expanded)) {
                Vec3 wallNormal = new Vec3(dir.getStepX(), 0, dir.getStepZ());
                double cross = lookVec.x * wallNormal.z - lookVec.z * wallNormal.x;
                double dot = lookVec.dot(wallNormal);
                bestLookAngle = (float) Math.toDegrees(Math.atan2(cross, dot));
                if (hasInput) {
                    cross = inputVec.x * wallNormal.z - inputVec.z * wallNormal.x;
                    dot = inputVec.dot(wallNormal);
                    bestInputAngle = (float) Math.toDegrees(Math.atan2(cross, dot));
                }
                bestDir = dir;

                // 收集墙面方块
                wallBlocks.clear();
                BlockPos.betweenClosedStream(expanded).forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        wallBlocks.add(pos.immutable());
                    }
                });
                break;
            }
        }
        if (bestDir == null) {
            this.hasFaceWall = false;
            this.hasLedge = false;
            this.wallDirection = null;
            this.wallNormal = Vec3.ZERO;
            this.lookWallAngle = 360F;
            this.inputWallAngle = 360F;
            return;
        }

        // 眼睛到头顶做个碰撞箱判断凹槽
        double eyeY = player.getEyeY();
        double topY = box.maxY; // 头顶
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;
        // 碰撞箱从玩家位置向墙面方向偏移一格
        AABB ledgeBox = new AABB(
                box.minX + bestDir.getStepX() * reach,
                eyeY,
                box.minZ + bestDir.getStepZ() * reach,
                box.maxX + bestDir.getStepX() * reach,
                topY,
                box.maxZ + bestDir.getStepZ() * reach
        );
        AABB chinBox = new AABB(
                box.minX + bestDir.getStepX() * reach,
                chinY,
                box.minZ + bestDir.getStepZ() * reach,
                box.maxX + bestDir.getStepX() * reach,
                eyeY,
                box.maxZ + bestDir.getStepZ() * reach
        );
        this.hasFaceWall = !level.noCollision(player, chinBox);
        this.hasLedge = level.noCollision(player, ledgeBox) && this.hasFaceWall;
        this.wallDirection = bestDir;
        this.wallNormal = new Vec3(bestDir.getStepX(), 0, bestDir.getStepZ());
        this.lookWallAngle = bestLookAngle;
        this.inputWallAngle = bestInputAngle;
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
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.2;

        float yaw = player.getYRot();
        Vec3 lookVec = new Vec3(
                -Math.sin(Math.toRadians(yaw)),
                0,
                Math.cos(Math.toRadians(yaw))
        ).normalize();

        Direction bestDir = null;
        float bestLookAngle = 360F;

        for (Direction dir : HORIZONTALS) {
            AABB expanded = box.expandTowards(
                    dir.getStepX() * reach,
                    0,
                    dir.getStepZ() * reach
            );

            if (!level.noCollision(player, expanded)) {
                Vec3 wallNormal = new Vec3(dir.getStepX(), 0, dir.getStepZ());
                double cross = lookVec.x * wallNormal.z - lookVec.z * wallNormal.x;
                double dot = lookVec.dot(wallNormal);
                bestLookAngle = (float) Math.toDegrees(Math.atan2(cross, dot));
                bestDir = dir;
            }
        }

        if (bestDir == null) {
            this.setWallDirection(null);
            this.setLookWallAngle(360F);
            this.setWallNormal(Vec3.ZERO);
            return;
        }

        this.setWallDirection(bestDir);
        this.setWallNormal(new Vec3(bestDir.getStepX(), 0, bestDir.getStepZ()));
        this.setLookWallAngle(bestLookAngle);
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

}