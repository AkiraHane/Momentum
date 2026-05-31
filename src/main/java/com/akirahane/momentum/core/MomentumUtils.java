package com.akirahane.momentum.core;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.MomentumClient;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class MomentumUtils {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    // 客户端，状态切换时调用
    public static void playStateAnimation(Player player, String animName, PlayerAnimationController controller) {
        if (controller != null) {
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(4, EasingType.EASE_IN_OUT_SINE),
                    Identifier.fromNamespaceAndPath(Momentum.MODID, animName)
            );
        } else {
            LOGGER.warn("播放动画失败, 没有找到玩家: {} 的动画控制器或控制器死了", player.getName().getString());
        }
    }

    // 停止动画
    public static void stopAnimation(Player player, PlayerAnimationController controller) {
        // 停止动画（4tick 淡出）
        if (controller != null && controller.isActive()) {
            controller.stop();
        } else {
            LOGGER.warn("停止动画失败, 没有找到玩家: {} 的动画控制器或控制器死了", player.getName().getString());
        }
    }

}
