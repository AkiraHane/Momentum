package com.akirahane.momentum.core.common.state.states;

import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

@Getter
public class MovementState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    protected final PlayerMovementContext data; // 玩家运动数据的引用

    public MovementState(PlayerMovementContext data) {
        this.data = data;
    }

    /**
     * 进入状态时调用一次
     */
    public void enter(MovementState previousState, Player player) {
        LOGGER.debug("[{}] enter", previousState.getClass().getSimpleName());

    }

    /**
     * 离开状态时调用一次
     */
    public void exit(MovementState nextState, Player player) {
        LOGGER.debug("[{}] exit", nextState.getClass().getSimpleName());
    }

    /**
     * 每tick调用，返回下一个状态（返回this表示不切换）
     */
    public MovementState tick(Player player, MovementState nowState) {
        data.syncFromPlayer(player);
        tickEffect(player);
        return toStateCheck(player, nowState);
    }

    public void tickEffect(Player player) {
    }

    public MovementState toStateCheck(Player player, MovementState nowState) {
        return newStateCheck(player, nowState, data);
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext data) {
        if (player.onGround() && !(nowState instanceof GroundState)) {
            return GroundState.newStateCheck(player, nowState, data);
        }
        return nowState;
    }


//    // 物理参数，子类按需覆写
//    public float getFriction() {
//        return -1;
//    }
//
//    public float getGravityMultiplier() {
//        return 1.0f;
//    }
//
//    public float getJumpPowerMultiplier() {
//        return 1.0f;
//    }
//
//    public float getFallDamageMultiplier() {
//        return 1.0f;
//    }
//
//    public Vec3 getVelocityOverride() {
//        return null;
//    }
//
//    public boolean isInvincible() {
//        return false;
//    }

    // 视觉

    /**
     * 摄像机倾斜角度，0为不倾斜
     */
    public float getCameraRoll() {
        return 0f;
    }

    /**
     * 客户端粒子
     */
    public void spawnParticles(Player player) {
    }

    /**
     * 循环音效
     */
    public void playLoopingSound(Player player) {
    }
}
