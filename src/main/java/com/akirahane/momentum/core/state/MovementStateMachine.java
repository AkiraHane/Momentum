package com.akirahane.momentum.core.state;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.effect.MomentumEffect;
import com.akirahane.momentum.core.effect.MomentumEffectType;
import com.akirahane.momentum.network.StateTransitionPacket;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.akirahane.momentum.core.state.StateType.ORIGINAL;

@Getter
@Setter
public class MovementStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<String, Method> METHOD_CACHE = new HashMap<>();

    private State currentState;

    @Getter
    protected final PlayerMovementContext context; // 玩家运动数据的引用

    public MovementStateMachine() {
        this.context = new PlayerMovementContext();
        currentState = ORIGINAL.getState();
    }

    // ==================== 生命周期 ====================

    public void clientTick(LocalPlayer player) {
        State next = currentState.tick(player, context);
        boolean needSend = transition(next, player);
        if (needSend) {
            ClientPacketDistributor.sendToServer(new StateTransitionPacket(next.getStateType()));
        }
    }

    public void serverTick(ServerPlayer player) {
        if (currentState == null) return;
        currentState.serverTick(player, context);
    }


    // ==================== 工具方法 ====================

    private Class<? extends State> findCommonAncestor(List<Class<? extends State>> chainA, List<Class<? extends State>> chainB) {
        for (Class<? extends State> a : chainA) {
            if (chainB.contains(a)) return a;
        }
        return State.class;
    }

    // ==================== 状态转换 ====================

    private boolean transition(State next, Player player) {
        if (next == null || next.getClass() == currentState.getClass()) return false;
        // 找到公共祖先
        List<Class<? extends State>> fromChain = currentState.getStateType().getAncestorChain();
        List<Class<? extends State>> toChain = next.getStateType().getAncestorChain();
        LOGGER.debug("[{}] ->> [{}] transition", currentState.getClass().getSimpleName(), next.getClass().getSimpleName());

        Class<? extends State> commonAncestor = findCommonAncestor(fromChain, toChain);

        // 从当前状态向上 exit，直到公共祖先（不包含公共祖先）
        for (Class<? extends State> clazz : fromChain) {
            if (clazz == commonAncestor) break;
            Method method = METHOD_CACHE.computeIfAbsent(clazz.getName() + "$onExit", key -> {
                try {
                    return clazz.getMethod("onExit", Player.class, PlayerMovementContext.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                LOGGER.debug("[{}] onExit", clazz.getSimpleName());
                method.invoke(null, player, context);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        // 从公共祖先向下 entry，直到目标状态
        List<Class<? extends State>> entryPath = new ArrayList<>(toChain.subList(0, toChain.indexOf(commonAncestor)));
        Collections.reverse(entryPath);
        for (Class<? extends State> clazz : entryPath) {
            Method method = METHOD_CACHE.computeIfAbsent(clazz.getName() + "$onEnter", key -> {
                try {
                    return clazz.getMethod("onEnter", Player.class, PlayerMovementContext.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                LOGGER.debug("[{}] onEnter", clazz.getSimpleName());
                method.invoke(null, player, context);
            } catch (InvocationTargetException | IllegalAccessException e) {
                // 如果报错, 看看是不是有子类没有覆盖父类的静态方法
                throw new RuntimeException(e);
            }
        }
        currentState = next;
        return true;
    }

    public void setStateFromClient(StateType newStateType, Player player) {
        transition(newStateType.getState(), player);
    }

}
