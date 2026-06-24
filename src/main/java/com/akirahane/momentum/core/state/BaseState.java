package com.akirahane.momentum.core.state;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.ProneState;
import com.akirahane.momentum.core.state.states.ground.SlideState;
import com.akirahane.momentum.core.state.states.ground.WalkState;
import com.akirahane.momentum.core.state.states.special.BreakFallState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.*;
import com.akirahane.momentum.core.state.states.water.SwimDashState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static com.akirahane.momentum.config.ServerConfig.*;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    protected static final Identifier WALL_GRAVITY_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "wall_gravity");
    // 上坡高度ID
    protected static final Identifier UP_SLOPE_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "up_slope");
    // 跳跃
    public static String JUMP_LEFT = "jump_left";
    public static String JUMP_RIGHT = "jump_right";
    public static String BACK_JUMP_LEFT = "back_jump_left";
    public static String BACK_JUMP_RIGHT = "back_jump_right";

    // 动画名称
    protected String IDLE = "idle";

    // ==================== 生命周期 ====================

    // 进入状态时调用一次。
    public void onEnter(Player player, PlayerMovementContext context) {
        playStateAnimation(player, IDLE, context);
    }

    // 服务器和客户端都支持的功能
    public void serverTick(Player player, PlayerMovementContext context) {
        if (MANEUVER_CONSUME_HUNGER.get()) {
            float modify = MANEUVER_CONSUME_HUNGER_AMOUNT.get().floatValue();
            if (context.isHasJetBooster()) {
                modify *= (1 - BOOSTER_STAMINA_REDUCTION.get().floatValue());
            }
            player.getFoodData().addExhaustion(modify);
        }
    }

    // 视觉效果和移动、状态转换相关内容
    public void clientTick(Player player, PlayerMovementContext context) {
        serverTick(player, context);
        clientTickRemote(player, context);
    }

    // 其他玩家要在客户端渲染视觉效果的所需数据
    public void clientTickRemote(Player player, PlayerMovementContext context) {
    }

    // 离开状态时调用一次。
    public void onExit(Player player, PlayerMovementContext context) {
        context.setTargetArmTransform(0, 0);
    }

    // 状态转换检查
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        HintManager.clear();
        HintManager.add(WallHangHints.ORIGINAL_STATE);
        HintManager.add(WallHangHints.TOGGLE_HINT);
        if (OriginalState.canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimDashState.canSwimDash(player, context)){
            return StateType.SWIM_DASH.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (BreakFallState.canBreakFall(player, context)) {
            return StateType.BREAK_FALL.getState();
        }
        if (VaultInState.canVaultIn(player, context)) {
            return StateType.VAULT_IN.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        if (WallKickState.canWallKick(player, context)) {
            return StateType.WALL_KICK.getState();
        }
        if (WallRunState.canWallRun(player, context)) {
            return StateType.WALL_RUN.getState();
        }
        if (VaultUpState.canVaultUp(player, context)) {
            return StateType.VAULT_UP.getState();
        }
        if (WallHangState.canWallHang(player, context)) {
            return StateType.WALL_HANG.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (BreakFallReadyState.canBreakFallReady(player, context)) {
            return StateType.BREAK_FALL_READY.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (WalkState.canWalk(player, context)) {
            return StateType.WALK.getState();
        }
        LOGGER.error("evaluate error! 有状态没有覆盖!");
        return StateType.ORIGINAL.getState();
    }

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
        if (ObjectUtils.isEmpty(animName) || animName.equals(context.getCurrentAnimationName())) {
            return;
        }
        if (context.getController() == null) {
            LOGGER.warn("播放动画失败, 没有找到玩家: {} 的动画控制器或控制器死了(什?)", player.getName().getString());
            return;
        }
        context.getController().removeModifierIf(m -> m instanceof AbstractFadeModifier);
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
