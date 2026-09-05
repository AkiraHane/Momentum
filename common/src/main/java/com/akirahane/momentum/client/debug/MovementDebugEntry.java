package com.akirahane.momentum.client.debug;

import com.akirahane.momentum.MomentumConstants;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.platform.PlatformServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

public final class MovementDebugEntry implements DebugScreenEntry {
    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, "movement_info");
    private static final Identifier GROUP =
            Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, "movement");

    @Override
    public void display(DebugScreenDisplayer displayer, Level level,
                        LevelChunk clientChunk, LevelChunk serverChunk) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        PlayerMovementContext context = PlatformServices.gameplay().movementState(player).getContext();
        Vec3 velocity = context.getSpeed().scale(20.0D);
        displayer.addToGroup(GROUP,
                String.format("[Momentum] H Speed * 20: %.4f", velocity.horizontalDistance()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] Y Vel * 20: %.4f", velocity.y));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] OnGround: %s", player.onGround()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] Fall Distance: %.4f", player.fallDistance));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] InputAngleToWall: %.4f", context.getInputWallAngle()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] LookAngleToWall: %.4f", context.getLookWallAngle()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] WallNormal: %s", context.getWallNormal()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] HasLedge: %s", context.isHasLedge()));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] JumpLimitSpeed: %.4f", context.getJumpLimitSpeed() * 20.0D));
        displayer.addToGroup(GROUP,
                String.format("[Momentum] JumpPower: %.4f", PlatformServices.gameplay().jumpPower(player)));
    }
}
