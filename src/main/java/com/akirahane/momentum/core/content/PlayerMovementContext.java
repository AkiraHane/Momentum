package com.akirahane.momentum.core.content;

import com.akirahane.momentum.core.compat.curios.CuriosCompat;
import com.akirahane.momentum.server.config.ServerConfig;
import com.akirahane.momentum.core.compat.curios.handler.CuriosHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
public class PlayerMovementContext {
    private float currentFriction;   // 当前摩擦系数，影响移动减速效果
    private int dodgeCooldown;       // 闪避冷却时间（tick数），冷却期间无法再次闪避
    private int airDodgeCount;       // 空中闪避次数（落地重置）
    private int wallContactTicks;    // 接触墙面的持续时间
    private int coyoteTimer;         // 离开地面/墙面后的宽容时间
    private boolean hasJetBooster;   // 是否装备喷射器
    private float stamina;           // 体力值
    private Vec3 wallNormal;         // 当前接触墙面的法线方向
    private Direction wallFace;      // 墙面朝向
    private boolean canMomentum;     // 是否能进行机动

    public PlayerMovementContext() {
    }
    // ==================== 每tick从Player同步 ====================

    /**
     * 在状态机tick之前调用，从Player读取最新数据
     */
    public void syncFromPlayer(Player player) {
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        // 碰撞检测
    }

    private boolean checkBoosterEquipped(Player player) {
        // 检查Curios腰饰槽（如果Curios存在）
        if (!CuriosCompat.isLoaded()) {
            return false;
        }
        return CuriosHandler.hasJetBooster(player);
    }

    private boolean checkMomentum(Boolean hasJetBooster) {
        if (ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.get()) {
            return true;
        } else {
            return hasJetBooster;
        }
    }
}