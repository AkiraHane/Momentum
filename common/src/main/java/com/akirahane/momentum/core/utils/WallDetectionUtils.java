package com.akirahane.momentum.core.utils;

import com.akirahane.momentum.core.context.PlayerMomentumContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class WallDetectionUtils {
    private static final Vec3[] WALL_CARDINALS = {
            new Vec3(0, 0, -1),  // NORTH
            new Vec3(0, 0, 1),   // SOUTH
            new Vec3(1, 0, 0),   // EAST
            new Vec3(-1, 0, 0),  // WEST
    };
    private static final Vec3[] WALL_DIAGONALS = {
            new Vec3(1, 0, 1).normalize(),
            new Vec3(1, 0, -1).normalize(),
            new Vec3(-1, 0, 1).normalize(),
            new Vec3(-1, 0, -1).normalize(),
    };

    // 方向顺序对应网络编号，调整顺序会改变协议。
    private static final Vec3[] ALL_WALL_NORMALS = {
            WALL_CARDINALS[0], WALL_CARDINALS[1], WALL_CARDINALS[2], WALL_CARDINALS[3],
            WALL_DIAGONALS[0], WALL_DIAGONALS[1], WALL_DIAGONALS[2], WALL_DIAGONALS[3],
    };

    public static int encodeWallNormal(Vec3 normal) {
        if (normal == null || normal.lengthSqr() < 0.001) return -1;
        for (int i = 0; i < ALL_WALL_NORMALS.length; i++) {
            if (ALL_WALL_NORMALS[i].distanceToSqr(normal) < 0.001) return i;
        }
        return -1;
    }

    public static Vec3 decodeWallNormal(int index) {
        if (index < 0 || index >= ALL_WALL_NORMALS.length) return Vec3.ZERO;
        return ALL_WALL_NORMALS[index];
    }

    // 墙面检测候选。
    private static class WallCandidate {
        Vec3 normal;
        // 用于收集墙面音效方块。
        AABB expanded;
        boolean hasFaceWall;
        boolean hasLedge;
        double ledgeOffsetY = 0; // 抓边高度补偿
        float lookAngle;        // 有符号，用于输出
        float inputAngle;       // 有符号，用于输出
        float lookAngleDiff;    // 无符号，用于排序
        float inputAngleDiff;   // 无符号，用于排序
    }

    public static void detectWall(Player player, PlayerMomentumContext context) {
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        Vec3 inputVec = context.getInputVec();
        boolean hasInput = inputVec.lengthSqr() > 0.001;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        // 优先四个正方向，再尝试斜向。
        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

        if (best == null) {
            List<WallCandidate> diagCands = collectWallCandidates(
                    player, level, box, WALL_DIAGONALS, lookVec, inputVec, inputNorm, hasInput, reach);
            best = pickByPriority(diagCands, hasInput);
        }

        if (best == null) {
            context.setHasFaceWall(false);
            context.setHasLedge(false);
            context.setLedgeOffsetY(0);
            context.setWallNormal(Vec3.ZERO);
            context.setLookWallAngle(360F);
            context.setInputWallAngle(360F);
            context.getWallBlocks().clear();
            return;
        }

        context.getWallBlocks().clear();
        BlockPos.betweenClosedStream(best.expanded).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                context.getWallBlocks().add(pos.immutable());
            }
        });
        context.setHasFaceWall(best.hasFaceWall);
        context.setHasLedge(best.hasLedge);
        context.setLedgeOffsetY(best.ledgeOffsetY);
        context.setWallNormal(best.normal);
        context.setLookWallAngle(best.lookAngle);
        context.setInputWallAngle(hasInput ? best.inputAngle : 360F);
    }

    private static List<WallCandidate> collectWallCandidates(
            Player player, Level level, AABB box, Vec3[] normals,
            Vec3 lookVec, Vec3 inputVec, Vec3 inputNorm, boolean hasInput, double reach) {
        List<WallCandidate> result = new ArrayList<>();

        double eyeY = player.getEyeY();
        double topY = box.maxY;
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;

        for (Vec3 normal : normals) {
            AABB expanded = box.expandTowards(normal.x * reach, 0, normal.z * reach);
            if (level.noCollision(player, expanded)) continue;

            WallCandidate c = new WallCandidate();
            c.normal = normal;
            c.expanded = expanded;

            // 视角角度
            double lookDot = lookVec.x * normal.x + lookVec.z * normal.z;
            double lookCross = lookVec.x * normal.z - lookVec.z * normal.x;
            c.lookAngle = (float) Math.toDegrees(Math.atan2(lookCross, lookDot));
            c.lookAngleDiff = (float) Math.toDegrees(
                    Math.acos(Math.min(1.0, Math.max(-1.0, lookDot))));

            // 输入角度
            if (hasInput) {
                double inputDot = inputVec.x * normal.x + inputVec.z * normal.z;
                double inputCross = inputVec.x * normal.z - inputVec.z * normal.x;
                c.inputAngle = (float) Math.toDegrees(Math.atan2(inputCross, inputDot));
                double normDot = inputNorm.x * normal.x + inputNorm.z * normal.z;
                c.inputAngleDiff = (float) Math.toDegrees(
                        Math.acos(Math.min(1.0, Math.max(-1.0, normDot))));
            } else {
                c.inputAngle = 360F;
                c.inputAngleDiff = 360F;
            }

            // 边缘检测
            AABB ledgeBox = new AABB(
                    box.minX + normal.x * reach, eyeY, box.minZ + normal.z * reach,
                    box.maxX + normal.x * reach, topY, box.maxZ + normal.z * reach
            );
            AABB chinBox = new AABB(
                    box.minX + normal.x * reach, chinY, box.minZ + normal.z * reach,
                    box.maxX + normal.x * reach, eyeY, box.maxZ + normal.z * reach
            );
            c.hasFaceWall = !level.noCollision(player, chinBox);
            c.hasLedge = level.noCollision(player, ledgeBox) && c.hasFaceWall;

            // 快速下落可能越过边缘，沿下落路径回溯检测。
            if (!c.hasLedge && c.hasFaceWall && player.getDeltaMovement().y < -0.4) {
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
                        c.hasLedge = true;
                        c.ledgeOffsetY = yOff;
                        break;
                    }
                }
            }

            result.add(c);
        }
        return result;
    }

    private static WallCandidate pickByPriority(List<WallCandidate> candidates, boolean hasInput) {
        if (candidates.isEmpty()) return null;

        // 优先可抓取边缘。
        List<WallCandidate> ledged = new ArrayList<>();
        for (WallCandidate c : candidates) {
            if (c.hasLedge) ledged.add(c);
        }
        List<WallCandidate> pool = !ledged.isEmpty() ? ledged : candidates;

        // 有输入时按输入方向选墙，否则按视线方向。
        WallCandidate best = null;
        float bestDiff = Float.MAX_VALUE;
        for (WallCandidate c : pool) {
            float diff = hasInput ? c.inputAngleDiff : c.lookAngleDiff;
            if (diff < bestDiff) {
                bestDiff = diff;
                best = c;
            }
        }
        return best;
    }

    public static void remoteDetectWall(Player player, PlayerMomentumContext context) {
        // 扫描墙面，无候选时回退到同步方向。
        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        // 以位移方向代替远程玩家的输入。
        Vec3 velocity = context.getSpeed();
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        Vec3 inputNorm = hasInput ? inputVec.normalize() : lookVec;

        List<WallCandidate> cardCands = collectWallCandidates(
                player, level, box, WALL_CARDINALS, lookVec, inputVec, inputNorm, hasInput, reach);
        WallCandidate best = pickByPriority(cardCands, hasInput);

        if (best == null) {
            List<WallCandidate> diagCands = collectWallCandidates(
                    player, level, box, WALL_DIAGONALS, lookVec, inputVec, inputNorm, hasInput, reach);
            best = pickByPriority(diagCands, hasInput);
        }

        if (best == null) {
            if (context.getTransitionWallData() >= 0) {
                applySyncedWallData(player, context);
                return;
            }
            context.setLookWallAngle(360F);
            context.setWallNormal(Vec3.ZERO);
            context.setInputWallAngle(360F);
            context.setHasFaceWall(false);
            context.setHasLedge(false);
            context.setLedgeOffsetY(0);
            return;
        }

        context.setWallNormal(best.normal);
        context.setLookWallAngle(best.lookAngle);
        context.setInputWallAngle(hasInput ? best.inputAngle : 360F);
        context.setHasFaceWall(best.hasFaceWall);
        context.setHasLedge(best.hasLedge);
        context.setLedgeOffsetY(best.ledgeOffsetY);
    }

    private static void applySyncedWallData(Player player, PlayerMomentumContext context) {
        int wallIndex = context.getTransitionWallData() & 0x7;

        Vec3 wallNormal = decodeWallNormal(wallIndex);
        if (Vec3.ZERO.equals(wallNormal)) {
            context.setWallNormal(Vec3.ZERO);
            context.setLookWallAngle(360F);
            context.setInputWallAngle(360F);
            context.setHasFaceWall(false);
            context.setHasLedge(false);
            context.setLedgeOffsetY(0);
            return;
        }

        AABB box = player.getBoundingBox();
        Level level = player.level();
        double reach = 0.5;

        // 计算视线角度
        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 lookVec = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        double lookDot = lookVec.x * wallNormal.x + lookVec.z * wallNormal.z;
        double lookCross = lookVec.x * wallNormal.z - lookVec.z * wallNormal.x;
        float lookAngle = (float) Math.toDegrees(Math.atan2(lookCross, lookDot));

        // 以速度方向估算输入夹角。
        Vec3 velocity = player.getDeltaMovement();
        Vec3 inputVec = new Vec3(velocity.x, 0, velocity.z);
        boolean hasInput = inputVec.horizontalDistanceSqr() > 1.0E-6;
        float inputAngle = 360F;
        if (hasInput) {
            double inputDot = inputVec.x * wallNormal.x + inputVec.z * wallNormal.z;
            double inputCross = inputVec.x * wallNormal.z - inputVec.z * wallNormal.x;
            inputAngle = (float) Math.toDegrees(Math.atan2(inputCross, inputDot));
        }

        // 只检测同步方向上的墙壁和边缘。
        AABB expanded = box.expandTowards(wallNormal.x * reach, 0, wallNormal.z * reach);
        double eyeY = player.getEyeY();
        double topY = box.maxY;
        double headHalf = topY - eyeY;
        double chinY = eyeY - headHalf * 2;
        AABB chinBox = new AABB(
                box.minX + wallNormal.x * reach, chinY, box.minZ + wallNormal.z * reach,
                box.maxX + wallNormal.x * reach, eyeY, box.maxZ + wallNormal.z * reach
        );
        AABB ledgeBox = new AABB(
                box.minX + wallNormal.x * reach, eyeY, box.minZ + wallNormal.z * reach,
                box.maxX + wallNormal.x * reach, topY, box.maxZ + wallNormal.z * reach
        );
        boolean hasFaceWall = !level.noCollision(player, chinBox);
        boolean hasLedge = level.noCollision(player, ledgeBox) && hasFaceWall;

        context.setWallNormal(wallNormal);
        context.setLookWallAngle(lookAngle);
        context.setInputWallAngle(hasInput ? inputAngle : 360F);
        context.setHasFaceWall(hasFaceWall);
        context.setHasLedge(hasLedge);
        context.setLedgeOffsetY(0);

        context.getWallBlocks().clear();
        BlockPos.betweenClosedStream(expanded).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) context.getWallBlocks().add(pos.immutable());
        });
    }
}
