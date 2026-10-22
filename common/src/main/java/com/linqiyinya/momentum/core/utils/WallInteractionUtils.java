package com.linqiyinya.momentum.core.utils;

import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.context.WallContact;
import com.linqiyinya.momentum.core.state.StateType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Predicate;

public class WallInteractionUtils {
    public static boolean selectWall(PlayerMomentumContext context, StateType action,
                                     float preferredAngle, Predicate<WallContact> condition) {
        // 默认保持同一动作已绑定的墙面；挂墙侧移通过另一重载显式允许切换。
        return selectWall(context, action, preferredAngle, condition, false);
    }

    /** 只记录临时候选；状态机确定动作后才提交为正在使用的墙面。 */
    public static boolean selectWall(PlayerMomentumContext context, StateType action,
                                     float preferredAngle, Predicate<WallContact> condition,
                                     boolean allowSwitch) {
        // 先根据动作条件、朝向偏好和保持规则，找出本次判定可用的墙面。
        WallContact wall = findWall(context, action, preferredAngle, condition, allowSwitch);
        // 判定可能因按键等其他条件失败，因此先保存试选结果，交由状态机在动作确定后提交。
        context.setPendingWall(wall);
        return wall != null;
    }

    /** 同一动作优先保持原墙；allowSwitch 允许按视角重新比较，供挂墙侧移使用。 */
    public static @Nullable WallContact findWall(PlayerMomentumContext context, StateType action,
                                                float preferredAngle, Predicate<WallContact> condition,
                                                boolean allowSwitch) {
        // 1. 只有同一动作才尝试保持原墙，并在本 tick 的候选中检查它是否仍满足条件。
        WallContact current = null;
        if (context.getActiveWallState() == action) {
            for (WallContact wall : context.getWallCandidates()) {
                if (wall.normal().equals(context.getWallNormal()) && condition.test(wall)) {
                    current = wall;
                    break;
                }
            }
            // 2. 持续动作直接返回原墙的检查结果；失效时返回空，由动作退出处理解绑。
            if (!allowSwitch) return current;
        }

        // 3. 新动作或允许切墙时，先过滤动作不可用的墙，再按朝向优先级选择最优候选。
        Comparator<WallContact> priority = WallDetectionUtils.priority(preferredAngle);
        WallContact best = context.getWallCandidates().stream().filter(condition).min(priority).orElse(null);
        // 4. 允许切墙也只在新墙更优时切换，同优先级时保持原墙，避免来回跳变。
        if (current != null && (best == null || priority.compare(current, best) <= 0)) return current;
        return best;
    }

    /** 一并提交动作归属、墙面数据和网络编号，供移动、声音与同步读取。 */
    public static void activateWall(PlayerMomentumContext context, StateType action, @Nullable WallContact wall) {
        // 提交已经确定的墙面和动作归属，后续移动、姿态与声音都读取这份数据。
        context.setActiveWall(wall);
        context.setActiveWallState(wall == null ? null : action);
        // 消费本次试选结果，并同步更新待发送的方向编号，保持本地绑定与网络数据一致。
        context.setPendingWall(null);
        context.setTransitionWallData(wallData(context));
    }

    public static void clearActiveWall(PlayerMomentumContext context) {
        // 用空墙面走同一提交入口，同时清除动作归属、试选结果和网络墙面编号。
        activateWall(context, null, null);
    }

    public static byte wallData(PlayerMomentumContext context) {
        // 网络只传绑定方向的编号；接收端用该方向重新补齐检测数据，空墙面编码为 -1。
        return (byte) WallDetectionUtils.encodeWallNormal(context.getWallNormal());
    }

    public static void applySyncedWall(Player player, PlayerMomentumContext context, StateType action, byte data) {
        // 先恢复同步墙面，再更新绑定；同状态切墙也使用此入口，保持动作进入效果连续。
        activateWall(context, action, wallFromData(player, data));
    }

    public static @Nullable WallContact wallFromData(Player player, byte data) {
        // 1. 从低三位恢复八方向编号；负数或无效编号表示没有绑定墙面。
        Vec3 normal = data < 0 ? Vec3.ZERO : WallDetectionUtils.decodeWallNormal(data & 0x7);
        if (Vec3.ZERO.equals(normal)) return null;
        // 2. 状态包未携带 WASD 输入，用水平速度估算远程玩家的输入夹角。
        Vec3 movement = player.getDeltaMovement();
        Vec3 input = new Vec3(movement.x, 0, movement.z);
        // 3. 依据接收端世界和玩家位置，补齐该方向的边缘、角度与声音方块数据。
        return WallDetectionUtils.describeWall(player, normal, input);
    }

    public static boolean hasSidewaysInput(Player player, PlayerMomentumContext context) {
        // 先用视角水平旋转确定侧向轴，再检查世界坐标输入在该轴上的分量。
        double yaw = Math.toRadians(player.getYRot());
        Vec3 input = context.getInputVec();
        // 斜向输入也可触发侧移；结果用于决定挂墙期间是否允许重新选墙。
        return Math.abs(input.x * Math.cos(yaw) + input.z * Math.sin(yaw)) > 0.01;
    }
}
