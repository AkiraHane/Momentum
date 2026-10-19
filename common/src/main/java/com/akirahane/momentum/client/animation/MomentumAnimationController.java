package com.akirahane.momentum.client.animation;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import team.unnamed.mocha.MochaEngine;

public class MomentumAnimationController extends PlayerAnimationController {
    private final SpeedModifier speedModifier;

    /** SpeedModifier.step() while 循环安全上限: 防止超大 speed 导致等效死循环 */
    public static final float MAX_SAFE_SPEED = 10f;

    public MomentumAnimationController(Avatar avatar, AnimationStateHandler animationHandler) {
        super(avatar, animationHandler);
        speedModifier = new SpeedModifier(1.0f);
        // ArmConditionModifier 放链首: 瞄准/攻击/使用物品时跳过手臂, 保留原版姿势
        // 仅对 Player 生效, 防止 ClientMannequin 等非 Player Avatar 导致 ClassCastException
        if (avatar instanceof Player player) {
            addModifierBefore(new ArmConditionModifier(player));
        }
        addModifierLast(speedModifier);
    }

    public MochaEngine<AnimationController> getMolangRuntime() {
        return this.molangRuntime;
    }

    public void setAnimationSpeed(float speed) {
        if (!Float.isFinite(speed) || speed <= 0f) {
            speed = 1.0f;
        }
        this.speedModifier.speed = speed;
    }

    /**
     * 在 PAL 每帧处理动画前封顶 speed，防止 SpeedModifier.step() 的 while 循环因
     * delta * speed 过大而等效死循环。
     */
    @Override
    public void tick(AnimationData state) {
        if (!Float.isFinite(speedModifier.speed) || speedModifier.speed > MAX_SAFE_SPEED) {
            speedModifier.speed = 1f;
        }
        super.tick(state);
    }

    /** @return 当前动画 tick 计数 (PAL 内部, 用于伺服控制器计算动画位置) */
    public int getAnimationTick() {
        return this.tick;
    }
}
