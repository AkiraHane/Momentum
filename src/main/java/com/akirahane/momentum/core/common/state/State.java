package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.common.state.base.BaseState;
import com.akirahane.momentum.core.common.state.states.OriginalState;
import com.akirahane.momentum.core.common.content.PlayerMovementContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class State extends BaseState {
    // ==================== 生命周期 ====================
    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }

    @Override
    public State tick(Player player, PlayerMovementContext context) {
        return super.tick(player, context);
    }

    @Override
    public void clientTick(LocalPlayer player, PlayerMovementContext context) {
        super.clientTick(player, context);
    }

    @Override
    public void serverTick(ServerPlayer player, PlayerMovementContext context) {
        super.serverTick(player, context);
    }
    // ==================== 状态转换检查 ====================

    /**
     * 检查父类层级的转换条件。
     * 如果当前状态不再满足父类的维持条件，返回父类期望的目标状态。
     * 返回 null 或 this 表示不需要跳转。
     */
    @Override
    protected State checkParentTransition(Player player, PlayerMovementContext context) {
        return super.checkParentTransition(player, context);
    }

    /**
     * 检查是否应该转换到更具体的子状态。
     * 会递归调用目标状态的 checkChildTransition，一次性跳转到最终目标。
     * 返回 null 表示没有子分支需要进入。
     */
    public static State checkChildTransition(Player player, PlayerMovementContext context) {
        return OriginalState.checkChildTransition(player, context);
    }

    public StateType getStateType() {
        return StateType.getStateType(this.getClass());
    }
}
