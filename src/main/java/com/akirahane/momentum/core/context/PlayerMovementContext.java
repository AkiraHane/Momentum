package com.akirahane.momentum.core.context;

import com.akirahane.momentum.core.MomentumUtils;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
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
    private boolean noMoveInput = false;     // 是否不接受移动输入
    private Vec3 speed = Vec3.ZERO;          // 移动速度
    private Vec3 dSpeed = Vec3.ZERO;     // 位移向量
    private boolean hasJetBooster = false;   // 是否装备喷射器
    private boolean canMomentum = true;     // 是否能进行机动
    private double blockStep = 0;        // 滑行上下升单位高度
    private Vec3 slopeUnitVector = Vec3.ZERO;    // 坡度加速向量

    // 从 mixin 中获取或计算的数据
    private float mixinVerticalFriction;     // 垂直光滑度
    private double mixinGravity;

    // 速度缓存
    private MomentumUtils.DoubleRingBuffer speedBuffer = new MomentumUtils.DoubleRingBuffer();

    private final Map<MomentumEffectType, MomentumEffect> effectMap;

    private final Map<MomentumEffectType, Set<MomentumEffect>> pendingEffectPool;

    public PlayerMovementContext() {
        this.resetMixinData();
        effectMap = new HashMap<>();
        pendingEffectPool = new HashMap<>();
        for (MomentumEffectType type : MomentumEffectType.values()) {
            effectMap.put(type, new MomentumEffect());
            pendingEffectPool.put(type, new HashSet<>());
        }
    }

    // 在状态机tick之前调用，从Player读取最新数据
    public void syncFromPlayer(Player player) {
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        speedBuffer.push(speed.y);
        this.dSpeed = player.getDeltaMovement();

        // 每tick要从Mixin中获取, 还有可能不会更新, 所以要每tick重置
        this.resetMixinData();
        LOGGER.debug("[State] SpeedXYZ: ({}, {} ,{} )", speed.x, speed.y, speed.z);
        LOGGER.debug("[State] 20 * HSpeed: {}", speed.horizontalDistance() * 20);
    }

    private void resetMixinData() {
        this.mixinVerticalFriction = 1.0F;
        this.mixinGravity = 0F;
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