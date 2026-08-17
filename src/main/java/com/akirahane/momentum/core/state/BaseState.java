package com.akirahane.momentum.core.state;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.OriginalState;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.air.BreakFallReadyState;
import com.akirahane.momentum.core.state.states.ground.PowerJumpState;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import static com.akirahane.momentum.client.animation.MomentumAnimationController.MAX_SAFE_SPEED;
import static com.akirahane.momentum.config.ServerConfig.*;

public abstract class BaseState {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();

    protected static final Identifier WALL_GRAVITY_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "wall_gravity");
    // 海豚跳重力
    protected static final Identifier DOLPHIN_GRAVITY_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "dolphin_gravity");
    // 上坡高度ID
    protected static final Identifier UP_SLOPE_ID =
            Identifier.fromNamespaceAndPath(Momentum.MODID, "up_slope");
    // 跳跃
    public static String JUMP_LEFT = "jump_left";
    public static String JUMP_RIGHT = "jump_right";
    public static String BACK_JUMP_LEFT = "back_jump_left";
    public static String BACK_JUMP_RIGHT = "back_jump_right";
    public static String FALL = "fall";

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
        if (DEFAULT_CHAIN == null) {
            DEFAULT_CHAIN = buildDefaultChain();
        }
        for (Transition t : transitionChain()) {
            BaseState next = t.tryTransition(player, context);
            if (next != null) {
                return next;
            }
        }
        LOGGER.error("evaluate error! 有状态没有覆盖!");
        return StateType.ORIGINAL.getState();
    }

    // ==================== 状态转换链（唯一默认链 + 每状态声明式微调） ====================

    // 状态转换规则：目标状态 + 进入条件。tryTransition 返回满足条件的下一状态，否则 null
    protected record Transition(StateType target, BiPredicate<Player, PlayerMovementContext> can) {
        BaseState tryTransition(Player player, PlayerMovementContext context) {
            return can.test(player, context) ? target.getState() : null;
        }
    }

    // 默认优先级链：全局唯一一份。子类通过 transitionChain() 声明式微调（移除/替换），无需手抄整条链。
    // 注意：不能用 static final 直接初始化——BaseState 会在 StateType 枚举构造期间被加载，
    // 此时 StateType.XXX 尚未赋值（循环初始化），会把链里 target 全存成 null，运行时 NPE。
    // 故延迟到首次 evaluate() 时构建（evaluate 仅客户端单线程调用，安全）。
    protected static List<Transition> DEFAULT_CHAIN;

    private static List<Transition> buildDefaultChain() {
        return List.of(
                new Transition(StateType.ORIGINAL, OriginalState::canOriginal),
                new Transition(StateType.DODGE, DodgeState::canDodge),
                new Transition(StateType.SWIM_DASH, SwimDashState::canSwimDash),
                new Transition(StateType.SLIDE, SlideState::canSlide),
                new Transition(StateType.BREAK_FALL, BreakFallState::canBreakFall),
                new Transition(StateType.VAULT_IN, VaultInState::canVaultIn),
                new Transition(StateType.SWIM, SwimState::canSwim),
                new Transition(StateType.PRONE, ProneState::canProne),
                new Transition(StateType.POWER_JUMP, PowerJumpState::canPowerJump),
                new Transition(StateType.WALL_KICK, WallKickState::canWallKick),
                new Transition(StateType.WALL_RUN, WallRunState::canWallRun),
                new Transition(StateType.VAULT_UP, VaultUpState::canVaultUp),
                new Transition(StateType.WALL_HANG, WallHangState::canWallHang),
                new Transition(StateType.WALL_CLIMB, WallClimbState::canWallClimb),
                new Transition(StateType.WALL_SLIDE, WallSlideState::canWallSlide),
                new Transition(StateType.BREAK_FALL_READY, BreakFallReadyState::canBreakFallReady),
                new Transition(StateType.AIRBORNE, AirborneState::canAirborne),
                new Transition(StateType.WALK, WalkState::canWalk)
        );
    }

    // 子类覆写，返回自己的转换链；默认使用全局唯一链
    protected List<Transition> transitionChain() {
        return DEFAULT_CHAIN;
    }

    // 微调辅助：从链中移除指定状态
    protected static List<Transition> without(List<Transition> chain, StateType... targets) {
        List<Transition> out = new ArrayList<>(chain);
        for (StateType t : targets) {
            out.removeIf(tr -> tr.target() == t);
        }
        return out;
    }

    // 微调辅助：替换指定状态的进入条件（常用于把"进入检查"换成"自保持检查"）
    protected static List<Transition> withPredicate(List<Transition> chain, StateType target,
                                                    BiPredicate<Player, PlayerMovementContext> can) {
        List<Transition> out = new ArrayList<>(chain);
        for (int i = 0; i < out.size(); i++) {
            if (out.get(i).target() == target) {
                out.set(i, new Transition(target, can));
            }
        }
        return out;
    }

    // 微调辅助：把 target 状态的检查移动到 anchor 之后（用于自保持检查需要比默认入口更早的情况，
    // 如受身/翻越动画期间不被滑铲/游泳等打断）。会先移除原 target，再在 anchor 后插入新谓词。
    protected static List<Transition> moveAfter(List<Transition> chain, StateType target, StateType anchor,
                                                BiPredicate<Player, PlayerMovementContext> can) {
        List<Transition> out = new ArrayList<>(chain);
        out.removeIf(tr -> tr.target() == target);
        for (int i = 0; i < out.size(); i++) {
            if (out.get(i).target() == anchor) {
                out.add(i + 1, new Transition(target, can));
                break;
            }
        }
        return out;
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
        if (context.getController() == null) {
            LOGGER.warn("播放动画失败, 没有找到玩家: {} 的动画控制器或控制器死了(什?)", player.getName().getString());
            return;
        }
        // 防护 PlayerAnimationLib SpeedModifier 死循环: 拒绝非法 speed 值
        if (!Float.isFinite(speed) || speed <= 0f) {
            speed = 1.0f;
        }
        if (speed > MAX_SAFE_SPEED) {
            speed = MAX_SAFE_SPEED;
        }
        context.getController().setAnimationSpeed(speed);
        if (ObjectUtils.isEmpty(animName) || animName.equals(context.getCurrentAnimationName())) {
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
