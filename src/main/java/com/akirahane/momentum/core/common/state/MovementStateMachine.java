package com.akirahane.momentum.core.common.state;

import com.akirahane.momentum.core.common.state.states.MovementState;
import com.akirahane.momentum.core.common.state.states.ground.GroundState;
import com.akirahane.momentum.core.content.PlayerMovementContext;
import com.akirahane.momentum.core.init.InitAttachments;
import com.akirahane.momentum.core.network.StateTransitionPacket;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.slf4j.Logger;

@Getter
@Setter
public class MovementStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    private MovementState currentState;

    public MovementStateMachine() {
        currentState = new GroundState(new PlayerMovementContext());
    }

    // ==================== 生命周期 ====================

    public void clientTick(LocalPlayer player) {
        MovementState next = currentState.tick(player, currentState);
        transition(next, player);

//        // 摄像机倾斜（跑墙）
//        CameraHandler.setTargetRoll(currentState.getCameraRoll());

        // 粒子效果（滑铲火花、蹬墙灰尘）
//        currentState.spawnParticles(player);
//
//        // 音效
//        currentState.playLoopingSound(player);
    }

    public void serverTick(ServerPlayer player) {
        if (currentState == null) return;
        // 服务端只关心无敌帧倒计时、耐久消耗等
        currentState.tickEffect(player);
    }


    // ==================== 状态转换 ====================

    private void transition(MovementState next, Player player) {
        if (next == null || next.getClass() == currentState.getClass()) return;
        LOGGER.debug("[{}] ->> [{}] transition", currentState.getClass().getSimpleName(), next.getClass().getSimpleName());

        currentState.exit(currentState, player);
        currentState = next;
        currentState.enter(currentState, player);
        ClientPacketDistributor.sendToServer(new StateTransitionPacket(next.getStateType()));
    }

    public void setStateFromClient(MovementStateType newStateType, Player player) {
        if (newStateType == null) return;
        transition(newStateType.getStateInstance(currentState.getContext()), player);
    }

    // ==================== 状态检查 ====================

}
