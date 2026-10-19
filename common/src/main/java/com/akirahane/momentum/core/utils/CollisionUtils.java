package com.akirahane.momentum.core.utils;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class CollisionUtils {
    public static boolean canPlayerFitAtPose(Player player, Pose pose) {
        EntityDimensions dimensions = player.getDimensions(pose);
        AABB boundingBox = dimensions.makeBoundingBox(player.position());
        return player.level().noCollision(player, boundingBox);
    }
}
