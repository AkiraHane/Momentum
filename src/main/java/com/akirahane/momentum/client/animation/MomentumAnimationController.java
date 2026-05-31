package com.akirahane.momentum.client.animation;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.animation.AnimationController;
import net.minecraft.world.entity.Avatar;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.Value;

public class MomentumAnimationController extends PlayerAnimationController {

    public MomentumAnimationController(Avatar avatar, AnimationStateHandler animationHandler) {
        super(avatar, animationHandler);
    }

    public MochaEngine<AnimationController> getMolangRuntime() {
        return this.molangRuntime;
    }

    public void setVariable(String name, Value value) {
        this.molangRuntime.scope().set(name, value);
    }
}
