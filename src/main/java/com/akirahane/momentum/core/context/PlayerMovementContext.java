package com.akirahane.momentum.core.context;

import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.PendingEffect;
import com.akirahane.momentum.core.enumerate.MomentumEffectType;
import com.akirahane.momentum.compat.curios.CuriosCompat;
import com.akirahane.momentum.config.ServerConfig;
import com.akirahane.momentum.compat.curios.handler.CuriosHandler;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

@Getter
@Setter
public class PlayerMovementContext {
    // 静态量
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 键位
    private static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, JUMP = 4;
    // 键位名称
    public static final String[] KEYS = {"up", "down", "left", "right", "jump"};
    // 墙面检测方向
    private static final Direction[] HORIZONTALS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    // 标志位
    // 是否降低重心
    private boolean lowerCenter = false;
    // 是否装备喷射器
    private boolean hasJetBooster = false;
    // 是否能进行机动
    private boolean canMomentum = true;
    // 是否不接受移动输入
    private boolean noMoveInput = false;
    // 是否禁止跳跃
    private boolean noJump = false;
    // 是否双击UP DOWN LEFT RIGHT JUMP
    private boolean doubleClickUp = false;
    private boolean doubleClickDown = false;
    private boolean doubleClickLeft = false;
    private boolean doubleClickRight = false;
    private boolean doubleClickJump = false;


    // 移动速度
    private Vec3 speed = Vec3.ZERO;
    // 上一Tick移动速度
    private Vec3 oldSpeed = Vec3.ZERO;
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
    float lookWallAngle = 0;
    // 向墙输入角度
    float inputWallAngle = 0;


    // 效果合计
    private final Map<MomentumEffectType, MomentumEffect> effectMap = new HashMap<>();
    // 输入缓冲角标
    private int inputBufferIndex = 0;
    // 输入缓冲
    private final Map<MomentumEffectType, Set<PendingEffect>> pendingEffectPool = new HashMap<>();
    @SuppressWarnings("unchecked")
    private final HashSet<String>[] inputBuffer = new HashSet[KEYS.length];

    // 每tick要变动的效果
    // 跳跃计数 用于跳跃限速
    private int jumpTimer = 0;
    // 滑铲冷却
    private int slideCooldown = 0;
    // 受身计时器
    private int breakFallTimer = 0;
    // 闪避计时器
    private int dodgeTimer = 0;

    public PendingEffect SLIDE_FRICTION = new PendingEffect(
            0, 0, 0.1F, 0, -1
    );
    public PendingEffect SLIDE_ACCELERATION = new PendingEffect(
            0, 0, 1.0F, 0, 0
    );
    public PendingEffect SLIDE_BLOCK_FRICTION = new PendingEffect(
            0, 0, 1.0F, 0, 0
    );

    public PlayerMovementContext() {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            effectMap.put(type, new MomentumEffect());
            pendingEffectPool.put(type, new HashSet<>());
        }
        for (int i = 0; i < KEYS.length; i++) {
            inputBuffer[i] = new HashSet<>();
        }
    }

    public void resetEffect() {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            effectMap.get(type).init();
            pendingEffectPool.get(type).clear();
        }
    }

    // 在状态机tick之前调用，从Player读取最新数据
    public void serverTick(Player player) {
        if (this.jumpTimer > 0) this.jumpTimer--;
        if (this.slideCooldown > 0) this.slideCooldown--;
        if (this.breakFallTimer > 0) this.breakFallTimer--;
        if (this.dodgeTimer > 0) this.dodgeTimer--;
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        if (player.fallDistance > 0) this.lastFallDistance = player.fallDistance;
    }

    public void clientTick(Player player) {
        this.serverTick(player);
        this.doubleClickUp = isDoubleClick(KEYS[UP]);
        this.doubleClickDown = isDoubleClick(KEYS[DOWN]);
        this.doubleClickLeft = isDoubleClick(KEYS[LEFT]);
        this.doubleClickRight = isDoubleClick(KEYS[RIGHT]);
        this.doubleClickJump = isDoubleClick(KEYS[JUMP]);
        this.setWorldInputVec(player);
        this.detectWall(player);
    }

    // 是否双击了某个键(两个true中至少隔一个false)
    public boolean isDoubleClick(String key) {
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
                for (String clickKey : inputBuffer[idx]) {
                    if (!inputBuffer[lastIdx].contains(clickKey)) {
                        // clickKey 是 这次 多出来的, 按下了其他键, 忽略判断和跳跃
                        if (!clickKey.equals("jump") && !clickKey.equals(key)) foundFirst = false;
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

    // 墙面数据判断
    public void detectWall(Player player) {
        AABB box = player.getBoundingBox();
        @SuppressWarnings("resource")
        Level level = player.level();
        double reach = 0.05;

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
        float bestLookAngle = Float.MAX_VALUE;
        float bestInputAngle = -1;

        for (Direction dir : HORIZONTALS) {
            AABB expanded = box.expandTowards(
                    dir.getStepX() * reach,
                    0,
                    dir.getStepZ() * reach
            );

            if (!level.noCollision(player, expanded)) {
                Vec3 wallNormal = new Vec3(dir.getStepX(), 0, dir.getStepZ());
                float lookAngle = (float) Math.toDegrees(Math.acos(lookVec.dot(wallNormal)));

                if (lookAngle < bestLookAngle) {
                    bestLookAngle = lookAngle;
                    bestDir = dir;
                    bestInputAngle = hasInput
                            ? (float) Math.toDegrees(Math.acos(inputNorm.dot(wallNormal)))
                            : -1;
                }
            }
        }
        if (bestDir == null) {
            this.wallDirection = null;
            this.wallNormal = Vec3.ZERO;
            this.lookWallAngle = -1;
            this.inputWallAngle = -1;
            return;
        }
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


}