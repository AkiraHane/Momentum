package com.akirahane.momentum.core.common.state.base;

import com.akirahane.momentum.core.common.state.State;
import com.akirahane.momentum.core.common.state.StateType;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    // ==================== 生命周期 ====================

    /**
     * 进入状态时调用一次。
     * 如果目标状态与当前状态的父类层级不同，会沿层级依次调用差异父类的 onEnter。
     */
    public abstract void onEnter(Player player, PlayerMovementContext context);

    /**
     * 离开状态时调用一次。
     * 如果目标状态与当前状态的父类层级不同，会沿层级依次调用差异父类的 onExit。
     */
    public abstract void onExit(Player player, PlayerMovementContext context);

    /**
     * 每tick调用，返回下一个状态（返回this表示不切换）
     */
    public State tick(Player player, PlayerMovementContext context) {
        context.syncFromPlayer(player);

        if (player instanceof LocalPlayer localPlayer) {
            clientTick(localPlayer, context);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverTick(serverPlayer, context);
        }

        // 先检查父类层级是否要求跳转（向上）
        State parentTarget = checkParentTransition(player, context);
        if (parentTarget != null && parentTarget.getClass() != this.getClass()) {
            return parentTarget;
        }

        // 再检查子分支是否有更具体的目标（向下）
        State childTarget = checkChildTransition(player, context);
        if (childTarget != null && childTarget.getClass() != this.getClass()) {
            return childTarget;
        }

        return null;
    }

    public void clientTick(LocalPlayer player, PlayerMovementContext context) {
    }

    public void serverTick(ServerPlayer player, PlayerMovementContext context) {
    }
    // ==================== 状态转换检查 ====================

    /**
     * 检查父类层级的转换条件。
     * 如果当前状态不再满足父类的维持条件，返回父类期望的目标状态。
     * 返回 null 或 this 表示不需要跳转。
     */
    protected State checkParentTransition(Player player, PlayerMovementContext context) {
        return null;
    }

    /**
     * 检查是否应该转换到更具体的子状态。
     * 会递归调用目标状态的 checkChildTransition，一次性跳转到最终目标。
     * 返回 null 表示没有子分支需要进入。
     */
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return State.checkChildTransition(player, context);
    }

    public abstract StateType getStateType();
}
