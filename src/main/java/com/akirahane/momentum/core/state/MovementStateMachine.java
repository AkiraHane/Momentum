package com.akirahane.momentum.core.state;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

import static com.akirahane.momentum.core.state.StateType.ORIGINAL;

@Getter
@Setter
public class MovementStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 当前状态
    private BaseState currentState;
    // 玩家数据上下文
    private final PlayerMovementContext context;
    // 是否需要广播状态
    private boolean dirty = false;

    public MovementStateMachine() {
        this.context = new PlayerMovementContext();
        currentState = ORIGINAL.getState();
        LOGGER.debug("[MovementStateMachine] init");
    }

    // ==================== 生命周期 ====================
    // 客户端 加服务端, 状态转换、实施效果、计算移动和视觉效果
    public BaseState clientTick(Player player) {
        handleEffect();
        BaseState next = currentState.evaluate((Player) player, context);
        boolean isTurn = transition(next, player);
        context.clientTick(player);
        currentState.clientTick((Player) player, context);
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
        dirty = true;
    }

    // 效果处理
    public void handleEffect() {
        for (Map.Entry<MomentumEffectType, Set<MomentumEffect>> entry :
                context.getPendingEffectPool().entrySet()) {
            Iterator<MomentumEffect> iterator = entry.getValue().iterator();
            // 因为可能涉及到清除自己, 用迭代
            while (iterator.hasNext()) {
                MomentumEffect momentumEffect = iterator.next();
                // -1 代表永久 需要添加的地方使用引用移除
                if (momentumEffect.getDuration() >= 0 &&
                        momentumEffect.getElapsedDuration() >= momentumEffect.getDuration()) {
                    iterator.remove();
                    continue;
                }
                // 自身衰减/变化
                momentumEffect.tick();
            }
        }
    }

    // 对数值进行生效
    public double applyEffect(double number, MomentumEffectType type) {
        Set<MomentumEffect> effects = context.getPendingEffectPool().get(type);
        if (effects == null || effects.isEmpty()) return number;

        // 排序：按照 EffectType 的优先级
        List<MomentumEffect> sorted = effects.stream()
                .sorted(Comparator.comparingInt(e -> e.getType().getPriority()))
                .toList();

        for (MomentumEffect effect : sorted) {
            number = effect.applyTo(number);
        }
        return number;
    }

    public Vec3 applyEffect(Vec3 vec, float yRot, MomentumEffectType type) {
        Set<MomentumEffect> effects = context.getPendingEffectPool().get(type);
        if (effects == null || effects.isEmpty()) return vec;

        List<MomentumEffect> sorted = effects.stream()
                .sorted(Comparator.comparingInt(e -> e.getType().getPriority()))
                .toList();

        for (MomentumEffect effect : sorted) {
            vec = effect.applyTo(vec, yRot);
        }
        return vec;
    }

}
