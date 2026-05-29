package com.akirahane.momentum.client.debug;

import com.akirahane.momentum.init.InitAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.akirahane.momentum.Momentum.MODID;

public class MovementDebugEntry implements DebugScreenEntry {

    private static final Identifier group =
            Identifier.fromNamespaceAndPath(MODID, "movement");

    @Override
    public void display(@NotNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 vel = mc.player.getData(InitAttachments.MOVEMENT_STATE).getContext().getSpeed();
        displayer.addToGroup(group, String.format("[Momentum] H Speed: %.4f", vel.horizontalDistance()));
        displayer.addToGroup(group, String.format("[Momentum] Y Vel: %.4f", vel.y));
        vel = vel.multiply(20, 20, 20);
        displayer.addToGroup(group, String.format("[Momentum] H Speed * 20: %.4f", vel.horizontalDistance()));
        displayer.addToGroup(group, String.format("[Momentum] Y Vel * 20: %.4f", vel.y));
        displayer.addToGroup(group, String.format("[Momentum] OnGround: %s", mc.player.onGround()));
        float yRot = mc.player.getYRot(); // 水平朝向 (yaw), -180 到 180
        float xRot = mc.player.getXRot(); // 垂直角度 (pitch), -90 到 90
        displayer.addToGroup(group, String.format("[Momentum] yRot: %.4f", yRot));
        displayer.addToGroup(group, String.format("[Momentum] xRot: %.4f", xRot));
        double fallDistance = mc.player.fallDistance;
        displayer.addToGroup(group, String.format("[Momentum] Fall Distance: %.4f", fallDistance));
    }
}
