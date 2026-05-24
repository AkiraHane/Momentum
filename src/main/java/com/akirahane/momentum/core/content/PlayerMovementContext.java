package com.akirahane.momentum.core.content;

import com.akirahane.momentum.core.compat.curios.CuriosCompat;
import com.akirahane.momentum.server.config.ServerConfig;
import com.akirahane.momentum.core.compat.curios.handler.CuriosHandler;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;

@Getter
@Setter
public class PlayerMovementContext {
    private int slopeHeight;         // 坡面高度差
    private boolean lowerCenter;     // 是否降低重心
    private int coyoteTimer;         // 离开地面/墙面后的宽容tick
    private boolean hasJetBooster;   // 是否装备喷射器
    private boolean canMomentum;     // 是否能进行机动

    public PlayerMovementContext() {
        this.slopeHeight = 0;
        this.lowerCenter = false;
        this.coyoteTimer = 0;
        this.hasJetBooster = false;
        this.canMomentum = true;
    }
    // ==================== 每tick从Player同步 ====================

    /**
     * 在状态机tick之前调用，从Player读取最新数据
     */
    public void syncFromPlayer(Player player) {
        this.hasJetBooster = checkBoosterEquipped(player);
        this.canMomentum = checkMomentum(this.hasJetBooster);
        // 碰撞检测
    }

    private boolean checkBoosterEquipped(Player player) {
        // 检查Curios腰饰槽（如果Curios存在）
        if (!CuriosCompat.isLoaded()) {
            return false;
        }
        return CuriosHandler.hasJetBooster(player);
    }

    private boolean checkMomentum(boolean hasJetBooster) {
        if (ServerConfig.ALLOW_MANEUVER_WITHOUT_BOOSTER.get()) {
            return true;
        } else {
            return hasJetBooster;
        }
    }

//    /**
//     * 根据前方地形坡度计算滑铲摩擦
//     *
//     * 下坡(-1格): divisor=1 → friction接近1.0（几乎无衰减）
//     * 平地( 0格): divisor=2 → friction中等
//     * 上坡(+1格): divisor=3 → friction较高衰减
//     * 中间角度线性插值
//     */
//    private float calculateSlideFriction(Player player, float baseFriction) {
//        Vec3 velocity = player.getDeltaMovement();
//        double horizontalSpeed = velocity.horizontalDistance();
//
//        // 速度太小无法判断方向，视为平地
//        if (horizontalSpeed * 20 < 0.001) {
//            float divisor = 2.0F;
//            return baseFriction + (1F - baseFriction) / divisor;
//        }
//
//        // 速度方向归一化
//        double dirX = velocity.x / horizontalSpeed;
//        double dirZ = velocity.z / horizontalSpeed;
//
//        // 当前站立位置的地表高度
//        BlockPos currentPos = player.blockPosition();
//        double currentSurface = findSurfaceHeight(player, currentPos);
//
//        // 前方一格的地表高度
//        BlockPos aheadPos = BlockPos.containing(
//                currentPos.getX() + dirX,
//                currentPos.getY(),
//                currentPos.getZ() + dirZ
//        );
//        double aheadSurface = findSurfaceHeight(player, aheadPos);
//
//        // 高度差：负=下坡，正=上坡
//        double heightDiff = aheadSurface - currentSurface;
//
//        // 限制在 [-1, 1] 范围内
//        heightDiff = Math.clamp(heightDiff, -1.0, 1.0);
//
//        // 线性映射：
//        // heightDiff = -1 (下坡45°) → divisor = 1
//        // heightDiff =  0 (平地)    → divisor = 2
//        // heightDiff = +1 (上坡45°) → divisor = 3
//        float divisor = (float) (2.0 + heightDiff);
//
//        return baseFriction + (1F - baseFriction) / divisor;
//    }
//    /**
//     * 找到指定位置的地表高度
//     * 从玩家脚部位置向下搜索第一个实心方块的顶部
//     */
//    private double findSurfaceHeight(Player player, BlockPos pos) {
//        Level level = player.level();
//        int startY = pos.getY();
//
//        // 从玩家位置向下搜索，最多搜索2格（滑铲吸附范围）
//        for (int y = startY; y >= startY - 2; y--) {
//            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
//            BlockState state = level.getBlockState(checkPos);
//
//            if (state.isSolidRender(level, checkPos)) {
//                // 返回方块顶部的Y坐标
//                VoxelShape shape = state.getCollisionShape(level, checkPos);
//                if (!shape.isEmpty()) {
//                    return y + shape.max(Direction.Axis.Y);
//                }
//                return y + 1.0;
//            }
//        }
//
//        // 下方没有实心方块，返回当前Y（视为悬空）
//        return pos.getY();
//    }
}