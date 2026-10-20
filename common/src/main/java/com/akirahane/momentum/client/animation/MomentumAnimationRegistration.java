package com.akirahane.momentum.client.animation;

import com.akirahane.momentum.Constants;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class MomentumAnimationRegistration {

    public static final Identifier MOMENTUM_ANIM =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "movement_anim");

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                MOMENTUM_ANIM, 1500,
                avatar -> {
                    if (avatar instanceof Player player) {
                        return new MomentumAnimationController(player,
                                (controller, state, animSetter) -> PlayState.STOP
                        );
                    }
                    return null;
                }
        );
    }
}
