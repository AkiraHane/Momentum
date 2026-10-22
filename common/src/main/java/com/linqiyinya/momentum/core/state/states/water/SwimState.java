package com.linqiyinya.momentum.core.state.states.water;

import com.linqiyinya.momentum.client.hud.HintManager;
import com.linqiyinya.momentum.client.hud.WallHangHints;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.core.state.BaseState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import static com.linqiyinya.momentum.client.input.LowerCenterKey.LOWER_CENTER;

public class SwimState extends BaseState {

    public static boolean canSwim(Player player, PlayerMomentumContext context) {
        if (player.isSwimming()) {
            return true;
        } else if (player.isUnderWater()) {
            if (player.isSprinting()) {
                HintManager.add(WallHangHints.SWIM_HOLD);
                return Minecraft.getInstance().options.keyUp.isDown();
            } else {
                HintManager.add(WallHangHints.SWIM_ACTIVE);
                return LOWER_CENTER.get().isDown() && Minecraft.getInstance().options.keyUp.isDown();
            }
        } else if (player.isInWater()) {
            HintManager.add(WallHangHints.SWIM_ACTIVE);
            return LOWER_CENTER.get().isDown() && Minecraft.getInstance().options.keyUp.isDown();
        }
        return false;
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 游泳状态下不能滑铲
        return without(DEFAULT_CHAIN, StateType.SLIDE);
    }

    public void onEnter(Player player, PlayerMomentumContext context) {
        super.onEnter(player, context);
        player.setSwimming(true);
        player.setSprinting(true);
        LOGGER.trace("player.isSwimming(): {}", player.isSwimming());
    }

    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
    }

    @Override
    public StateType getStateType() {
        return StateType.SWIM;
    }
}
