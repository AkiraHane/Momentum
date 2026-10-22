package com.linqiyinya.momentum.core.state.states.wall;

import com.linqiyinya.momentum.client.config.ClientConfig;
import com.linqiyinya.momentum.client.hud.HintManager;
import com.linqiyinya.momentum.client.hud.WallHangHints;
import com.linqiyinya.momentum.config.ServerConfig;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.context.WallContact;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.core.state.StateType;
import com.linqiyinya.momentum.core.utils.WallInteractionUtils;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.linqiyinya.momentum.client.input.LowerCenterKey.LOWER_CENTER;

public class VaultInState extends BaseState {
    // 动画名称
    public static final String VAULT_IN = "vault_in";

    public static boolean canVaultIn(Player player, PlayerMomentumContext context) {
        return ServerConfig.ENABLE_VAULT_IN.getAsBoolean() && ClientConfig.ENABLE_VAULT_IN.getAsBoolean() &&
                !Vec3.ZERO.equals(context.getInputVec()) &&
                WallInteractionUtils.selectWall(context, StateType.VAULT_IN, 0,
                        wall -> (wall.hasLedge() || player.onGround() && !wall.hasFaceWall()) &&
                                Mth.abs(wall.inputAngle()) < 90 && checkKey(player, wall));
    }

    public static boolean checkKey(Player player, WallContact wall) {
        if (player.onGround() && !wall.hasFaceWall()){
            HintManager.add(WallHangHints.VAULT_IN_STAND);
            return Minecraft.getInstance().options.keyUp.isDown() &&
                    LOWER_CENTER.get().isDown()
                    ;
        } else {
            HintManager.add(WallHangHints.VAULT_IN);
            return LOWER_CENTER.get().isDown() &&
                    Minecraft.getInstance().options.keyJump.isDown();
        }
    }

    @Override
    public void onEnter(Player player, PlayerMomentumContext context) {
        Services.PLATFORM.setForcedPose(player, Pose.SWIMMING);
        context.setVaultTimer(10);
        context.setNoJump(true);
        if (player.onGround()){
            var instance = player.getAttribute(Attributes.STEP_HEIGHT);
            if (instance != null && instance.getModifier(UP_SLOPE_ID) == null) {
                instance.addOrReplacePermanentModifier(new AttributeModifier(
                        UP_SLOPE_ID,
                        0.6,
                        AttributeModifier.Operation.ADD_VALUE
                ));
            }
        } else {
            player.setDeltaMovement(
                    player.getDeltaMovement().x,
                    0.6,
                    player.getDeltaMovement().z
            );
        }
        playStateAnimation(player, VAULT_IN, context, 2, 1.5F);
    }

    @Override
    public void onExit(Player player, PlayerMomentumContext context) {
        super.onExit(player, context);
        var instance = player.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.removeModifier(UP_SLOPE_ID);
        }
        Services.PLATFORM.setForcedPose(player, null);
        context.setNoJump(false);
    }

    @Override
    protected java.util.List<Transition> transitionChain() {
        // 翻入持续期自保持，且比默认入口更早（紧跟 SwimDash），避免翻越期间被打断；翻入时不再考虑上翻
        return moveAfter(
                without(DEFAULT_CHAIN, StateType.VAULT_UP),
                StateType.VAULT_IN, StateType.SWIM_DASH,
                (p, c) -> c.getVaultTimer() > 0);
    }

    @Override
    public StateType getStateType() {
        return StateType.VAULT_IN;
    }
}
