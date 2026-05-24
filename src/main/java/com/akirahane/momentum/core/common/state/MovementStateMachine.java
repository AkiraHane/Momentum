package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.core.network.StateTransitionPacket;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.akirahane.momentum.core.common.state.StateType.ORIGINAL;

@Getter
@Setter
public class MovementStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

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
        transition(next, player);
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

    private void transition(State next, Player player) {
        if (next == null || next.getClass() == currentState.getClass()) return;
        // 找到公共祖先
        List<Class<? extends State>> fromChain = currentState.getStateType().getAncestorChain();
        List<Class<? extends State>> toChain = next.getStateType().getAncestorChain();
        LOGGER.debug("[{}] ->> [{}] transition", currentState.getClass().getSimpleName(), next.getClass().getSimpleName());

        Class<? extends State> commonAncestor = findCommonAncestor(fromChain, toChain);

        // 从当前状态向上 exit，直到公共祖先（不包含公共祖先）
        for (Class<? extends State> clazz : fromChain) {
            if (clazz == commonAncestor) break;
            StateType.getStateType(clazz).getState().onExit(player, context);
        }

        // 从公共祖先向下 entry，直到目标状态
        List<Class<? extends State>> entryPath = new ArrayList<>(toChain.subList(0, toChain.indexOf(commonAncestor)));
        Collections.reverse(entryPath);
        for (Class<? extends State> clazz : entryPath) {
            StateType.getStateType(clazz).getState().onEnter(player, context);
        }
        currentState = next;

        ClientPacketDistributor.sendToServer(new StateTransitionPacket(next.getStateType()));
    }

    public void setStateFromClient(StateType newStateType, Player player) {
        if (newStateType == null) return;
        transition(newStateType.getState(), player);
    }

}
