package com.akirahane.momentum.platform;

import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;

public interface ForcedPoseAccess {
    void momentum$setForcedPose(@Nullable Pose pose);
}
