package com.akirahane.momentum.core.state.base;

import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    // ==================== 生命周期 ====================

    // 进入状态时调用一次。
    public void onEnter(Player player, PlayerMovementContext context) {
    }

    // 服务器和客户端都支持的功能
    public void serverTick(Player player, PlayerMovementContext context) {
    }

    // 觉效果和移动、状态转换相关内容
    public void clientTick(LocalPlayer player, PlayerMovementContext context) {
    }

    // 离开状态时调用一次。
    public void onExit(Player player, PlayerMovementContext context) {
    }

    // 状态转换检查
    public BaseState evaluate(LocalPlayer player, PlayerMovementContext context) {
        if (!(player.getData(InitAttachments.MOMENTUM_ENABLED) && context.isCanMomentum())
                || player.getAbilities().flying    // 飞行
                || player.isFallFlying()           // 鞘翅
                || player.isPassenger()            // 骑乘
                || player.onClimbable()            // 爬梯子
                || player.isSleeping()             // 睡觉
                || player.isSpectator()            // 旁观者
                || player.isAutoSpinAttack()       // 旋转攻击(三叉戟)
                || player.isDeadOrDying()          // 死亡
        ) {
            return StateType.ORIGINAL.getState();
        }
        return null;
    }

    public abstract StateType getStateType();
}
