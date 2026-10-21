package com.akirahane.momentum.core.utils;

import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.core.context.WallContact;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WallDetectionUtils {
    // 顺序对应现有网络编号。
    private static final Vec3[] WALL_NORMALS = {
            new Vec3(0, 0, -1),
            new Vec3(0, 0, 1),
            new Vec3(1, 0, 0),
            new Vec3(-1, 0, 0),
            new Vec3(1, 0, 1).normalize(),
            new Vec3(1, 0, -1).normalize(),
            new Vec3(-1, 0, 1).normalize(),
            new Vec3(-1, 0, -1).normalize(),
    };

    public static int encodeWallNormal(Vec3 normal) {
        // 将有效朝墙向量匹配到固定的八方向表，供状态包传输；无法匹配时使用 -1。
        if (normal == null || normal.lengthSqr() < 0.001) return -1;
        for (int i = 0; i < WALL_NORMALS.length; i++) {
            if (WALL_NORMALS[i].distanceToSqr(normal) < 0.001) return i;
        }
        return -1;
    }

    public static Vec3 decodeWallNormal(int index) {
        // 按同一张方向表还原向量，无效编号统一表示为空墙面。
        return index < 0 || index >= WALL_NORMALS.length ? Vec3.ZERO : WALL_NORMALS[index];
    }

    public static void detectWall(Player player, PlayerMomentumContext context) {
        // 先保存本 tick 的完整候选，供每个动作根据自身条件独立选择。
        context.setWallCandidates(collectWalls(player, context.getInputVec(), true));
        // 已绑定的墙面只刷新检测数据；选墙和解绑由动作判定、状态切换负责。
        refreshActiveWall(context);
    }

    public static void remoteDetectWall(Player player, PlayerMomentumContext context) {
        // 远程玩家没有本地 WASD 输入，以水平位移估算方向，采集用于显示的候选数据。
        Vec3 speed = context.getSpeed();
        Vec3 input = new Vec3(speed.x, 0, speed.z);
        context.setWallCandidates(collectWalls(player, input, false));
        if (context.getActiveWall() != null) {
            // 互动目标遵循同步方向，在接收端刷新它的角度、边缘与声音数据，保持双方目标一致。
            context.setActiveWall(describeWall(player, context.getWallNormal(), input));
        }
    }

    private static List<WallContact> collectWalls(Player player, Vec3 input, boolean allowSweep) {
        // 常规检测使用当前位置，与提前探测共用方向筛选和碰撞规则。
        return collectWalls(player, input, allowSweep, Vec3.ZERO);
    }

    public static List<WallContact> detectWallsAtOffset(Player player, Vec3 input, Vec3 offset) {
        // 平移检测区域来预判移动后的墙面，保持玩家位置及上下文中的实际选墙结果不变。
        // 预测挂墙延续性只检查该位置的边缘，关闭快速下落时的向上回查。
        return collectWalls(player, input, false, offset);
    }

    private static List<WallContact> collectWalls(Player player, Vec3 input, boolean allowSweep, Vec3 offset) {
        // 1. 先检测四个正方向，为斜向排除提供依据，同时保留所有命中的墙面。
        List<WallContact> walls = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            WallContact wall = inspectWall(player, WALL_NORMALS[i], input, true, allowSweep, offset);
            if (wall != null) walls.add(wall);
        }

        // 2. 固定这批正方向结果，再逐个检查未被相邻正方向遮蔽的斜向。
        List<WallContact> cardinals = List.copyOf(walls);
        for (int i = 4; i < WALL_NORMALS.length; i++) {
            Vec3 normal = WALL_NORMALS[i];
            // 相邻正方向与斜向的点积约为 0.707，其余为负；任一相邻墙存在就排除斜向。
            if (cardinals.stream().anyMatch(wall -> wall.normal().dot(normal) > 0.5)) continue;
            WallContact wall = inspectWall(player, normal, input, true, allowSweep, offset);
            if (wall != null) walls.add(wall);
        }
        // 3. 按通用的面朝优先顺序排序，返回只读快照，供本 tick 的动作判定共享。
        walls.sort(priority(0));
        return List.copyOf(walls);
    }

    /** 先比较方向组，再比较组内角度；preferredAngle 正对为 0，背对为 180。 */
    public static Comparator<WallContact> priority(float preferredAngle) {
        // 先满足正向、侧向等方向组的优先级；同组内再选更接近动作目标朝向的墙。
        return Comparator.comparingInt((WallContact wall) -> directionPriority(wall, preferredAngle))
                .thenComparingDouble(wall -> angleDifference(wall, preferredAngle));
    }

    private static int directionPriority(WallContact wall, float preferredAngle) {
        // 先换算墙面偏离动作目标朝向的角度，再结合正方向/斜向划分优先组。
        double angle = angleDifference(wall, preferredAngle);
        boolean diagonal = wall.normal().x != 0 && wall.normal().z != 0;
        // 0～4 依次为目标正向、左右、背向、斜前、斜后；45°/135° 为正方向分组边界。
        if (diagonal) return angle <= 90 ? 3 : 4;
        if (angle < 45) return 0;
        return angle <= 135 ? 1 : 2;
    }

    private static double angleDifference(WallContact wall, float preferredAngle) {
        // 排序时左右同等对待，比较墙面与正对或背对目标的接近程度；原始夹角仍保留左右符号。
        return Math.abs(Math.abs(wall.lookAngle()) - preferredAngle);
    }

    private static void refreshActiveWall(PlayerMomentumContext context) {
        if (context.getActiveWall() == null) return;
        // 在新候选中寻找原绑定方向，用最新角度和边缘数据替换上一 tick 的检测结果。
        for (WallContact wall : context.getWallCandidates()) {
            if (wall.normal().equals(context.getWallNormal())) {
                context.setActiveWall(wall);
                return;
            }
        }
        // 未命中仍保留最后数据，供墙跳和翻越的固定计时阶段使用。
        // 持续贴墙动作会因新候选中缺少原墙而退出，届时统一清除绑定。
    }

    /** 同步方向可能刚离开检测范围，仍需按该方向恢复角度和声音数据。 */
    public static WallContact describeWall(Player player, Vec3 normal, Vec3 input) {
        // 两个 false 分别关闭“必须仍接触墙”和“快速下落回查”，用于还原已经同步确定的墙面。
        return inspectWall(player, normal, input, false, false);
    }

    private static @Nullable WallContact inspectWall(
            Player player, Vec3 normal, Vec3 input, boolean requireCollision, boolean allowSweep) {
        // 现有动作与同步还原均从玩家当前位置检测。
        return inspectWall(player, normal, input, requireCollision, allowSweep, Vec3.ZERO);
    }

    private static @Nullable WallContact inspectWall(
            Player player, Vec3 normal, Vec3 input, boolean requireCollision, boolean allowSweep, Vec3 offset) {
        // 1. 沿给定方向扩展玩家碰撞箱，常规检测要求该范围确实存在碰撞。
        // 同步还原允许目标暂时离开范围，仍按指定方向生成角度和环境数据。
        AABB box = player.getBoundingBox().move(offset);
        Level level = player.level();
        double reach = 0.5;
        AABB expanded = box.expandTowards(normal.x * reach, 0, normal.z * reach);
        if (requireCollision && level.noCollision(player, expanded)) return null;

        // 2. 按眼睛高度划分上下检测区域，分别检查墙体支撑和可抓握边缘。
        double eyeY = player.getEyeY() + offset.y;
        double topY = box.maxY;
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;
        // 眼睛上方留空、下方有墙时，判为可抓握边缘。
        AABB ledgeBox = new AABB(
                box.minX + normal.x * reach, eyeY, box.minZ + normal.z * reach,
                box.maxX + normal.x * reach, topY, box.maxZ + normal.z * reach
        );
        AABB chinBox = new AABB(
                box.minX + normal.x * reach, chinY, box.minZ + normal.z * reach,
                box.maxX + normal.x * reach, eyeY, box.maxZ + normal.z * reach
        );
        boolean hasFaceWall = !level.noCollision(player, chinBox);
        boolean hasLedge = level.noCollision(player, ledgeBox) && hasFaceWall;
        double ledgeOffsetY = 0;

        // 3. 快速下落可能跨过边缘，按当前下落速度扩大向上回查范围，记录挂墙的位置补偿。
        if (allowSweep && !hasLedge && hasFaceWall && player.getDeltaMovement().y < -0.4) {
            double fallDist = Math.abs(player.getDeltaMovement().y);
            for (double yOff = 0.2; yOff <= fallDist + 0.5; yOff += 0.2) {
                AABB sweepLedgeBox = new AABB(
                        box.minX + normal.x * reach, eyeY + yOff, box.minZ + normal.z * reach,
                        box.maxX + normal.x * reach, topY + yOff, box.maxZ + normal.z * reach
                );
                AABB sweepChinBox = new AABB(
                        box.minX + normal.x * reach, chinY + yOff, box.minZ + normal.z * reach,
                        box.maxX + normal.x * reach, eyeY + yOff, box.maxZ + normal.z * reach
                );
                if (level.noCollision(player, sweepLedgeBox) && !level.noCollision(player, sweepChinBox)) {
                    hasLedge = true;
                    ledgeOffsetY = yOff;
                    break;
                }
            }
        }

        // 4. 将视线和移动输入分别与朝墙方向比较，供动作筛选、方向排序和动画使用。
        double yaw = Math.toRadians(player.getYRot());
        Vec3 look = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        float lookAngle = angle(look, normal);
        float inputAngle = input.horizontalDistanceSqr() > 0.001 ? angle(input, normal) : 360F;
        // 5. 收集该方向检测范围内的声音方块，与几何、角度数据一起封装成单面墙的结果。
        List<BlockPos> blocks = new ArrayList<>();
        BlockPos.betweenClosedStream(expanded).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) blocks.add(pos.immutable());
        });
        return new WallContact(normal, hasFaceWall, hasLedge, ledgeOffsetY, lookAngle, inputAngle, blocks);
    }

    private static float angle(Vec3 direction, Vec3 normal) {
        // 先计算点积和水平叉积，再用 atan2 得到有左右符号的角度，供转向与动作判断使用。
        double dot = direction.x * normal.x + direction.z * normal.z;
        double cross = direction.x * normal.z - direction.z * normal.x;
        return (float) Math.toDegrees(Math.atan2(cross, dot));
    }
}
