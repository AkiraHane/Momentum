package com.akirahane.momentum.core.state;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.PendingEffect;
import com.akirahane.momentum.core.enumerate.MomentumEffectType;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.*;

import static com.akirahane.momentum.core.enumerate.StateType.ORIGINAL;

@Getter
@Setter
public class MovementStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 当前状态
    private BaseState currentState;
    // 玩家数据上下文
    private final PlayerMovementContext context;

    public MovementStateMachine() {
        this.context = new PlayerMovementContext();
        currentState = ORIGINAL.getState();
        LOGGER.debug("[MovementStateMachine] init");
    }

    // ==================== 生命周期 ====================
    // 客户端 加服务端, 状态转换、实施效果、计算移动和视觉效果
    public BaseState clientTick(Player player) {
        handleEffect();
        BaseState next = currentState.evaluate((LocalPlayer) player, context);
        boolean isTurn = transition(next, player);
        context.clientTick(player);
        currentState.clientTick((LocalPlayer) player, context);
        if (isTurn) {
            return next;
        }
        return null;
    }

    // 服务端实施效果
    public void serverTick(Player player) {
        context.serverTick(player);
        currentState.serverTick(player, context);
    }

    // 状态转换
    private boolean transition(BaseState next, Player player) {
        if (next.equals(currentState)) return false;
        LOGGER.debug("[MovementStateMachine] {} to {}", currentState.getStateType(), next.getStateType());
        currentState.onExit(player, context);
        currentState = next;
        currentState.onEnter(player, context);
        return true;
    }

    // 客户端向服务端同步状态
    public void setStateFromClient(StateType newStateType, Player player) {
        transition(newStateType.getState(), player);
    }

    // 效果处理
    public void handleEffect() {
        for (Map.Entry<MomentumEffectType, Set<PendingEffect>> entry :
                context.getPendingEffectPool().entrySet()) {
            MomentumEffect target = context.getEffectMap().get(entry.getKey());
            target.init();

            Iterator<PendingEffect> iterator = entry.getValue().iterator();
            // 因为可能涉及到清除自己, 用迭代
            while (iterator.hasNext()) {
                PendingEffect pendingEffect = iterator.next();
                // -1 代表永久 需要添加的地方使用引用移除
                if (pendingEffect.getDuration() == 0) {
                    iterator.remove();
                    continue;
                }
                // 累加
                target.setValue(target.getValue() + pendingEffect.getValue());
                target.setMultiplier(target.getMultiplier() * pendingEffect.getMultiplier());
                // 自身衰减/变化
                pendingEffect.setValue(pendingEffect.getValue() + pendingEffect.getModifyValue());
                pendingEffect.setMultiplier(pendingEffect.getMultiplier() + pendingEffect.getModifyMultiplier());
                if (pendingEffect.getDuration() > 0) {
                    pendingEffect.setDuration(pendingEffect.getDuration() - 1);
                }
            }
        }
    }

}
