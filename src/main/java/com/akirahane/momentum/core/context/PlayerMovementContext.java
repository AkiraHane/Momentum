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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

@Getter
@Setter
public class PlayerMovementContext {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    private boolean lowerCenter = false;     // 是否降低重心
    private boolean hasJetBooster = false;   // 是否装备喷射器
    private boolean canMomentum = true;     // 是否能进行机动

    // 移动速度
    private Vec3 speed = Vec3.ZERO;
    // 上一Tick移动速度
    private Vec3 oldSpeed = Vec3.ZERO;
    // 滑行上下单位高度
    private double blockStep = 0;
    // 坡度加速向量
    private Vec3 slopeUnitVector = Vec3.ZERO;
    // 是否不接受移动输入
    private boolean noMoveInput = false;
    // 是否禁止跳跃
    private boolean noJump = false;
    // 上次掉落的数据
    private double lastFallDistance = 0;
    // 效果合计
    private final Map<MomentumEffectType, MomentumEffect> effectMap;
    // 待处理效果
    private final Map<MomentumEffectType, Set<PendingEffect>> pendingEffectPool;

    // 每tick要变动的效果
    // 跳跃计数 用于跳跃限速
    private int jumpBuffer = 0;
    // 滑铲冷却
    private int slideCooldown = 0;
    // 是否处于受身
    private int breakFallBuffer = 0;

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
        effectMap = new HashMap<>();
        pendingEffectPool = new HashMap<>();
        for (MomentumEffectType type : MomentumEffectType.values()) {
            effectMap.put(type, new MomentumEffect());
            pendingEffectPool.put(type, new HashSet<>());
        }
    }

    public void resetEffect() {
        for (MomentumEffectType type : MomentumEffectType.values()) {
            effectMap.get(type).init();
            pendingEffectPool.get(type).clear();
        }
    }

    // 在状态机tick之前调用，从Player读取最新数据
    public void tick(Player player) {
        if (this.jumpBuffer > 0) this.jumpBuffer--;
        if (this.slideCooldown > 0) this.slideCooldown--;
        if (this.breakFallBuffer > 0) this.breakFallBuffer--;
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        if (player.fallDistance > 0) this.lastFallDistance = player.fallDistance;
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


}