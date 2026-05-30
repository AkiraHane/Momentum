package com.akirahane.momentum.core;

import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.core.enumerate.StateType;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.init.InitAttachments;
import com.akirahane.momentum.config.ServerConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

import static com.akirahane.momentum.core.enumerate.MomentumEffectType.ACCELERATION;

public class MomentumUtils {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    public static float getAirFriction(Player player) {
        MovementStateMachine stateMachine = player.getData(InitAttachments.MOVEMENT_STATE);
        if (stateMachine.getCurrentState().getStateType().equals(StateType.ORIGINAL)) {
            return 0.91F;
        }
        // =================== 内容 ===================
        return ServerConfig.AIR_FRICTION.get().floatValue();
    }
}
