package com.linqiyinya.momentum.core.state.states;

import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import static com.linqiyinya.momentum.config.ServerConfig.MANEUVER_CONSUME_HUNGER;
import static com.linqiyinya.momentum.core.context.PlayerMomentumContext.DEFAULT_FRICTION;
import static com.linqiyinya.momentum.core.effect.MomentumEffectType.FRICTION;

public class OriginalState extends BaseState {
    public static boolean canOriginal(Player player, PlayerMomentumContext context) {
        return !(Services.PLATFORM.isMomentumEnabled(player) && context.isCanMomentum())
                || player.getAbilities().flying    // 飞行
                || player.isFallFlying()           // 鞘翅
                || player.isPassenger()            // 骑乘
                || player.isSleeping()             // 睡觉
                || player.isSpectator()            // 旁观者
                || player.isAutoSpinAttack()       // 旋转攻击(三叉戟)
                || player.isDeadOrDying()          // 死亡
                || MANEUVER_CONSUME_HUNGER.get() && player.getFoodData().getFoodLevel() <= 6.0F
                ;
    }

    @Override
    public void onEnter(Player player, PlayerMomentumContext context) {
        context.resetEffect();
        playStateAnimation(player, IDLE, context);
        stopAnimation(player, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.removeModifier(UP_SLOPE_ID);
        }
    }

    @Override
    public void serverTick(Player player, PlayerMomentumContext context) {
        // 原版状态不额外消耗饱食度
    }

    @Override
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        context.resetEffect();
        stopAnimation(player, context);
        var instance = player.getAttribute(Attributes.GRAVITY);
        if (instance != null) {
            instance.removeModifier(WALL_GRAVITY_ID);
        }
        instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.removeModifier(UP_SLOPE_ID);
        }
        context.addPermanentEffect(FRICTION, DEFAULT_FRICTION);
    }

    @Override
    public StateType getStateType() {
        return StateType.ORIGINAL;
    }
}
