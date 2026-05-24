package com.akirahane.momentum.core.common.state.states;

import com.akirahane.momentum.core.common.state.MovementStateType;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public abstract class MovementState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    @Getter
    protected final PlayerMovementContext context; // 玩家运动数据的引用

    public MovementState(PlayerMovementContext context) {
        this.context = context;
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
        context.syncFromPlayer(player);
        tickEffect(player);
        return toStateCheck(player, nowState);
    }

    public void tickEffect(Player player) {
    }

    public MovementState toStateCheck(Player player, MovementState nowState) {
        return newStateCheck(player, nowState, context);
    }

    public static MovementState newStateCheck(Player player, MovementState nowState, PlayerMovementContext data) {
        if (player.onGround() && !(nowState instanceof GroundState)) {
            return GroundState.newStateCheck(player, nowState, data);
        }
        return nowState;
    }

    public abstract MovementStateType getStateType();
}
