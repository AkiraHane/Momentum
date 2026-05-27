package com.akirahane.momentum.core.common.context;

import com.akirahane.momentum.core.common.effect.MomentumEffect;
import com.akirahane.momentum.core.common.effect.MomentumEffectType;
import com.akirahane.momentum.core.compat.curios.CuriosCompat;
import com.akirahane.momentum.server.config.ServerConfig;
import com.akirahane.momentum.core.compat.curios.handler.CuriosHandler;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

import static com.akirahane.momentum.core.common.MomentumUtils.getClearVec3;

@Getter
@Setter
public class PlayerMovementContext {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    private boolean lowerCenter;     // 是否降低重心
    private Vec3 speed;              // 移动速度
    private boolean hasJetBooster;   // 是否装备喷射器
    private boolean canMomentum;     // 是否能进行机动
    private double blockStep;        // 滑行上下升单位高度

    // 从 mixin 中获取的数据
    float mixinVerticalFriction;     // 垂直光滑度
    double mixinGravity;

    private Map<MomentumEffectType, MomentumEffect> effectMap;

    private Map<MomentumEffectType, Set<MomentumEffect>> pendingEffectPool;

    public PlayerMovementContext() {
        this.lowerCenter = false;
        this.hasJetBooster = false;
        this.canMomentum = true;
        this.blockStep = 0;
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
        this.speed = getClearVec3(player.getDeltaMovement().add(
                0,
                player.onGround() ? this.mixinGravity * this.mixinVerticalFriction : 0,
                0
        ));

        // 每tick要从Mixin中获取, 还有可能不会更新, 所以要每tick重置
        this.resetMixinData();
//        LOGGER.debug("[State] OSpeedXYZ: {}", player.getDeltaMovement());
//        LOGGER.debug("[State] getGravity * 0.98F: {}", player.getGravity() * 0.98F);
        LOGGER.debug("[State] 20 * SpeedXYZ: ({}, {} ,{} )", speed.x * 20, speed.y * 20, speed.z * 20);
        LOGGER.debug("[State] 20 * Speed: {}", speed.horizontalDistance() * 20);
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