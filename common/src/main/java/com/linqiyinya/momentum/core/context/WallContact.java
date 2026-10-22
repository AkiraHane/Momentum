package com.linqiyinya.momentum.core.context;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 一面候选墙的检测结果；normal 指向墙面，角度为水平方向的有符号夹角。 */
public record WallContact(
        // 从玩家指向墙面的单位向量，也用于辨认和同步八个检测方向。
        Vec3 normal,
        // 下巴到眼睛高度的检测区域是否有墙。
        boolean hasFaceWall,
        // 下方有墙且眼睛上方留空，表示存在可抓握边缘。
        boolean hasLedge,
        // 快速下落回查找到的边缘高度补偿，进入挂墙时据此将玩家向上拉回。
        double ledgeOffsetY,
        // 视线与朝墙方向的水平夹角（度），符号区分左右，0 为朝墙、180 为背墙。
        float lookAngle,
        // 移动输入与朝墙方向的水平夹角（度）；360 表示没有有效输入方向。
        float inputAngle,
        // 该方向检测范围内的非空气方块，供当前墙面的声音采样使用。
        List<BlockPos> blocks
) {
    public WallContact {
        blocks = List.copyOf(blocks);
    }
}
