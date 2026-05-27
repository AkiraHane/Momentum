package com.akirahane.momentum.core.state;

import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class State extends BaseState {
    // ==================== 生命周期 ====================
    public static void onEnter(Player player, PlayerMovementContext context) {
    }

    public static void onExit(Player player, PlayerMovementContext context) {
    }

    @Override
    public State tick(Player player, PlayerMovementContext context) {
        LOGGER.debug(String.valueOf(context.getEffectMap().get(MomentumEffectType.ACCELERATION).getValue()));
        LOGGER.debug(String.valueOf(context.getEffectMap().get(MomentumEffectType.ACCELERATION).getMultiplier()));
        for (Map.Entry<MomentumEffectType, Set<MomentumEffect>> entry :
                context.getPendingEffectPool().entrySet()) {
            MomentumEffect target = context.getEffectMap().get(entry.getKey());
            target.init();

            Iterator<MomentumEffect> iterator = entry.getValue().iterator();
            // 因为可能涉及到清除自己, 用迭代
            while (iterator.hasNext()) {
                MomentumEffect momentumEffect = iterator.next();
                // -1 代表永久 需要添加的地方使用引用移除
                if (momentumEffect.getDuration() == 0) {
                    iterator.remove();
                    continue;
                }
                // 累加
                target.setValue(target.getValue() + momentumEffect.getValue());
                target.setMultiplier(target.getMultiplier() * momentumEffect.getMultiplier());
                // 自身衰减/变化
                momentumEffect.setValue(momentumEffect.getValue() + momentumEffect.getModifyValue());
                momentumEffect.setMultiplier(momentumEffect.getMultiplier() + momentumEffect.getModifyMultiplier());
                if (momentumEffect.getDuration() > 0) {
                    momentumEffect.setDuration(momentumEffect.getDuration() - 1);
                }
            }
        }
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
     * 检查是否应该转换到更具体的子状态。
     * 会递归调用目标状态的 checkChildTransition，一次性跳转到最终目标。
     * 返回 null 表示没有子分支需要进入。
     */
    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        return OriginalState.checkChildTransition(player, context, nowState);
    }

    public StateType getStateType() {
        return StateType.getStateType(this.getClass());
    }
}
