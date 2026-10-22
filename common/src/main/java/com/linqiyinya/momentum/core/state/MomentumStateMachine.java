package com.linqiyinya.momentum.core.state;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.context.WallContact;
import com.linqiyinya.momentum.core.effect.MomentumEffect;
import com.linqiyinya.momentum.core.effect.MomentumEffectType;
import com.linqiyinya.momentum.core.utils.MomentumTickUtils;
import com.linqiyinya.momentum.core.utils.WallInteractionUtils;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.*;

import static com.linqiyinya.momentum.core.state.StateType.ORIGINAL;

@Getter
@Setter
public class MomentumStateMachine {
    // 日志
    protected static final Logger LOGGER = LogUtils.getLogger();
    // 当前状态
    private BaseState currentState;
    // 玩家数据上下文
    private final PlayerMomentumContext context;
    // 是否需要广播状态
    private boolean dirty = false;

    public MomentumStateMachine(Player player) {
        this.context = new PlayerMomentumContext(player);
        currentState = ORIGINAL.getState();
        LOGGER.trace("[MovementStateMachine] init");
        if (Constants.LOG.isDebugEnabled()) {
            Constants.LOG.debug("[DATA] INITIALIZE side={} player={} name={} uuid={} dimension={} context={}",
                    player.level().isClientSide() ? "client" : "server", player.getId(), player.getName().getString(),
                    player.getUUID(), player.level().dimension().identifier(), System.identityHashCode(context));
        }
    }

    // ==================== 生命周期 ====================
    // 客户端 加服务端, 状态转换、实施效果、计算移动和视觉效果
    public BaseState clientTick(Player player) {
        handleEffect();
        BaseState org = currentState;
        Vec3 previousWall = context.getWallNormal();
        MomentumTickUtils.clientTick(player, context);
        currentState.clientTick(player, context);
        BaseState next = currentState.evaluate(player, context);
        transition(next, player);
        if (!StateType.SLIDE.equals(currentState.getStateType())){
            context.setSlopeUnitVector(Vec3.ZERO);
        }
        // 同一挂墙状态切换墙面时，也要返回动作供客户端上报。
        if (!org.equals(next) || !previousWall.equals(context.getWallNormal())) {
            context.setTransitionWallData(WallInteractionUtils.wallData(context));
            return next;
        }
        return null;
    }

    // 服务端实施效果
    public void serverTick(Player player) {
        MomentumTickUtils.serverTick(player, context);
        currentState.serverTick(player, context);
    }

    // 客户端远程玩家tick
    public void clientTickRemote(Player player) {
        MomentumTickUtils.clientTickRemote(player, context);
        currentState.clientTickRemote(player, context);
    }

    // 状态转换
    private void transition(BaseState next, Player player) {
        WallContact selectedWall = context.getPendingWall();
        if (next.equals(currentState)) {
            // 持续挂墙只更新目标，保持进入效果和动作计时连续。
            if (selectedWall != null) {
                Vec3 previousWall = context.getWallNormal();
                WallInteractionUtils.activateWall(context, next.getStateType(), selectedWall);
                if (!previousWall.equals(selectedWall.normal())) {
                    Constants.LOG.debug("[WALL] SWITCH side={} player={} from={} to={}",
                            player.level().isClientSide() ? "client" : "server", player.getId(),
                            previousWall, selectedWall.normal());
                }
            }
            return;
        }
        LOGGER.trace("[MovementStateMachine] {} to {}", currentState.getStateType(), next.getStateType());
        context.setPreviousStateType(currentState.getStateType());
        // 退出钩子使用旧墙；清理绑定后，再让进入钩子使用本次选中的墙。
        currentState.onExit(player, context);
        WallInteractionUtils.clearActiveWall(context);
        currentState = next;
        WallInteractionUtils.activateWall(context, next.getStateType(), selectedWall);
        currentState.onEnter(player, context);
        Constants.LOG.debug("[STATE] TRANSITION side={} player={} dimension={} from={} to={} extra={} wall={}",
                player.level().isClientSide() ? "client" : "server", player.getId(), player.level().dimension().identifier(),
                context.getPreviousStateType(), currentState.getStateType(), context.getTransitionExtraData(), context.getTransitionWallData());
    }

    // 客户端向服务端同步状态
    public void setStateFromClient(StateType newStateType, Player player) {
        byte wallData = context.getTransitionWallData();
        if (currentState.getStateType() == newStateType) {
            // 挂墙切墙只更新目标，不重复执行进入动作和附加效果。
            WallInteractionUtils.applySyncedWall(player, context, newStateType, wallData);
            dirty = true;
            return;
        }
        context.setPendingWall(WallInteractionUtils.wallFromData(player, wallData));
        transition(newStateType.getState(), player);
        dirty = true;
    }

    // 效果处理
    public void handleEffect() {
        for (Map.Entry<MomentumEffectType, Set<MomentumEffect>> entry :
                context.getPendingEffectPool().entrySet()) {
            Iterator<MomentumEffect> iterator = entry.getValue().iterator();
            // 因为可能涉及到清除自己, 用迭代
            while (iterator.hasNext()) {
                MomentumEffect momentumEffect = iterator.next();
                // -1 代表永久 需要添加的地方使用引用移除
                if (momentumEffect.getDuration() >= 0 &&
                        momentumEffect.getElapsedDuration() >= momentumEffect.getDuration()) {
                    momentumEffect.setElapsedDuration(0);
                    iterator.remove();
                    continue;
                }
                // 自身衰减/变化
                momentumEffect.tick();
            }
        }
    }

    // 对数值进行生效
    public double applyEffect(double number, MomentumEffectType type) {
        Set<MomentumEffect> effects = context.getPendingEffectPool().get(type);
        if (effects == null || effects.isEmpty()) return number;
        double origin = number;
        // 按优先级排序 (effect 数量通常 ≤ 3, 直接排比维护额外缓存更轻量)
        MomentumEffect[] sorted = effects.toArray(MomentumEffect[]::new);
        java.util.Arrays.sort(sorted, (a, b) -> Integer.compare(a.getType().getPriority(), b.getType().getPriority()));
        for (MomentumEffect effect : sorted) {
            number = effect.applyTo(number);
        }
        return Double.isFinite(number) ? number : origin;
    }

    public Vec3 applyEffect(Vec3 vec, float yRot, MomentumEffectType type) {
        Set<MomentumEffect> effects = context.getPendingEffectPool().get(type);
        if (effects == null || effects.isEmpty()) return vec;
        Vec3 origin = vec;

        MomentumEffect[] sorted = effects.toArray(MomentumEffect[]::new);
        java.util.Arrays.sort(sorted, (a, b) -> Integer.compare(a.getType().getPriority(), b.getType().getPriority()));
        for (MomentumEffect effect : sorted) {
            vec = effect.applyTo(vec, yRot);
            if (vec == null) return origin;
        }
        return (Double.isFinite(vec.x) && Double.isFinite(vec.y) && Double.isFinite(vec.z))
                ? vec
                : origin;
    }

}
