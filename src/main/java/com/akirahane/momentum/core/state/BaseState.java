package com.akirahane.momentum.core.state;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    protected static final Identifier WALL_GRAVITY_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "wall_gravity");

    // 动画名称
    protected String IDLE = "idle";

    // ==================== 生命周期 ====================

    // 进入状态时调用一次。
    public void onEnter(Player player, PlayerMovementContext context) {
        playStateAnimation(player, IDLE, context);
    }

    // 服务器和客户端都支持的功能
    public void serverTick(Player player, PlayerMovementContext context) {
        if (context.getDeltaLastFallDistance() > 0) {
            double cap = context.getDeltaLastFallDistance() * 5.0; // 伤害和速度成正比, 需要调试
            if (player.fallDistance > cap) {
                player.fallDistance = cap;
            }
        }
    }

    // 觉效果和移动、状态转换相关内容
    public void clientTick(Player player, PlayerMovementContext context) {
    }

    // 离开状态时调用一次。
    public void onExit(Player player, PlayerMovementContext context) {
    }

    // 状态转换检查
    public abstract BaseState evaluate(Player player, PlayerMovementContext context);

    public abstract StateType getStateType();


    // 客户端，状态切换时调用
    public static void playStateAnimation(Player player, @NotNull String animName, PlayerMovementContext context) {
        playStateAnimation(player, animName, context, 6, 1.0f);
    }

    public static void playStateAnimation(Player player, @NotNull String animName, PlayerMovementContext context, int fadeInTime, float speed) {
        if (!player.level().isClientSide()) {
            return;
        }
        context.getController().setAnimationSpeed(speed);
        if (animName.equals(context.getCurrentAnimationName())) {
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
