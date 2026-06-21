package com.akirahane.momentum.core.state.states;

import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.init.InitAttachments;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.context.PlayerMovementContext.DEFAULT_FRICTION;
import static com.akirahane.momentum.core.effect.MomentumEffectType.FRICTION;

public class OriginalState extends BaseState {
    public static boolean canOriginal(Player player, PlayerMovementContext context) {
        return !(player.getData(InitAttachments.MOMENTUM_ENABLED) && context.isCanMomentum())
                || player.getAbilities().flying    // 飞行
                || player.isFallFlying()           // 鞘翅
                || player.isPassenger()            // 骑乘
                || player.isSleeping()             // 睡觉
                || player.isSpectator()            // 旁观者
                || player.isAutoSpinAttack()       // 旋转攻击(三叉戟)
                || player.isDeadOrDying()          // 死亡
                || player.getFoodData().getFoodLevel() <= 6.0F
                ;
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        context.resetEffect();
        playStateAnimation(player, IDLE, context);
        stopAnimation(player, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
    }

    @Override
    public void serverTick(Player player, PlayerMovementContext context) {
        // 原版状态不额外消耗饱食度
    }

    @Override
    public void onExit(Player player, PlayerMovementContext context) {
        super.onExit(player, context);
        context.resetEffect();
        stopAnimation(player, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        context.addPermanentEffect(FRICTION, DEFAULT_FRICTION);
    }

    @Override
    public StateType getStateType() {
        return StateType.ORIGINAL;
    }
}
