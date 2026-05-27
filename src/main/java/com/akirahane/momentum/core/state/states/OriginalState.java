package com.akirahane.momentum.core.state.states;

import com.akirahane.momentum.core.state.State;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.base.BaseState;
import com.akirahane.momentum.init.InitAttachments;
import net.minecraft.world.entity.player.Player;

public class OriginalState extends State {
    public static State checkChildTransition(Player player, PlayerMovementContext context, BaseState nowState) {
        if (player.getData(InitAttachments.MOMENTUM_ENABLED)
                && !player.getAbilities().flying    // 飞行
                && !player.isFallFlying()           // 鞘翅
                && !player.isPassenger()            // 骑乘
                && !player.onClimbable()            // 爬梯子
                && !player.isSleeping()             // 睡觉
                && !player.isSpectator()            // 旁观者
                && !player.isAutoSpinAttack()       // 旋转攻击(三叉戟)
                && !player.isDeadOrDying()          // 死亡
        ) {
            return MovementState.checkChildTransition(player, context, nowState);
        }
        return StateType.ORIGINAL.getState();
    }
}
