package com.akirahane.momentum.client.animation;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import net.minecraft.world.entity.Avatar;
import team.unnamed.mocha.MochaEngine;

public class MomentumAnimationController extends PlayerAnimationController {
    private final SpeedModifier speedModifier;

    public MomentumAnimationController(Avatar avatar, AnimationStateHandler animationHandler) {
        super(avatar, animationHandler);
        speedModifier = new SpeedModifier(1.0f);
        addModifierLast(speedModifier);
    }

    public MochaEngine<AnimationController> getMolangRuntime() {
        return this.molangRuntime;
    }

    public void setAnimationSpeed(float speed) {
        this.speedModifier.speed = speed;
    }

    /** @return 当前动画 tick 计数 (PAL 内部, 用于伺服控制器计算动画位置) */
    public int getAnimationTick() {
        return this.tick;
    }
}
