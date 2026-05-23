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

    // ========== 从Player读取的数据（每tick开头同步一次） ==========
    private Vec3 velocity;           // 玩家速度
    private Vec3 position;           // 玩家位置
    private Vec3 lookDirection;      // 玩家当前视线朝向的方向向量
    private float yaw;               // 玩家水平旋转角度（偏航角）
    private float pitch;             // 玩家垂直旋转角度（俯仰角）
    private boolean onGround;        // 玩家是否站在地面上
    private boolean inWater;         // 玩家是否在水中
    private double distanceToGround; // 玩家距离地面的垂直距离
    private float horizontalSpeed;   // 玩家水平移动速度的大小

    // ========== 状态机自己维护的数据 ==========
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
        this.velocity = player.getDeltaMovement();
        this.position = player.position();
        this.lookDirection = player.getLookAngle();
        this.yaw = player.getYRot();
        this.pitch = player.getXRot();
        this.onGround = player.onGround();
        this.inWater = player.isInWater();
        this.horizontalSpeed = (float) velocity.horizontalDistance();
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        // 碰撞检测
    }

    /**
     * 在状态机tick之后调用，把结果写回Player
     */
    public void syncToPlayer(Player player) {
        player.setDeltaMovement(velocity);
        // 姿态、碰撞箱等也在这里写回
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