package com.akirahane.momentum.core.state;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    // 动画名称
    protected String IDLE = "idle";

    // ==================== 生命周期 ====================

    // 进入状态时调用一次。
    public void onEnter(Player player, PlayerMovementContext context) {
        playStateAnimation(player, IDLE, context);
    }

    // 服务器和客户端都支持的功能
    public void serverTick(Player player, PlayerMovementContext context) {
    }

    // 觉效果和移动、状态转换相关内容
    public void clientTick(Player player, PlayerMovementContext context) {
    }

    // 离开状态时调用一次。
    public void onExit(Player player, PlayerMovementContext context) {
    }

    // 状态转换检查
    public BaseState evaluate(Player player, PlayerMovementContext context) {
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


    // 客户端，状态切换时调用
    public static void playStateAnimation(Player player, @NotNull String animName, PlayerMovementContext context) {
        playStateAnimation(player, animName, context, 4);
    }

    public static void playStateAnimation(Player player, @NotNull String animName, PlayerMovementContext context, int fadeInTime) {
        if (!player.level().isClientSide() || animName.equals(context.getCurrentAnimationName())) {
            return;
        }
        if (context.getController() == null) {
            LOGGER.warn("播放动画失败, 没有找到玩家: {} 的动画控制器或控制器死了(什?)", player.getName().getString());
            return;
        }
        context.getController().replaceAnimationWithFade(
                AbstractFadeModifier.standardFadeIn(fadeInTime, EasingType.EASE_IN_OUT_SINE),
                Identifier.fromNamespaceAndPath(Momentum.MODID, animName)
        );
        context.setCurrentAnimationName(animName);
    }

    // 停止动画
    public static void stopAnimation(Player player, PlayerMovementContext context) {
        if (!player.level().isClientSide() || context.getCurrentAnimationName() == null) {
            return;
        }
        if (context.getController() == null) {
            LOGGER.warn("停止动画失败, 没有找到玩家: {} 的动画控制器或控制器死了(什?)", player.getName().getString());
            return;
        }
        context.getController().stop();
        context.setCurrentAnimationName(null);
    }
}
