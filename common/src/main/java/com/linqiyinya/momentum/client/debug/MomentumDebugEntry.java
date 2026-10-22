package com.linqiyinya.momentum.client.debug;

import com.linqiyinya.momentum.core.context.PlayerMomentumContext;
import com.linqiyinya.momentum.core.context.WallContact;
import com.linqiyinya.momentum.platform.Services;
import com.linqiyinya.momentum.mixin.LivingEntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.linqiyinya.momentum.Constants.MOD_ID;

public class MomentumDebugEntry implements DebugScreenEntry {

    public static final Identifier ENTRY_ID =
            Identifier.fromNamespaceAndPath(MOD_ID, "movement_info");

    private static final Identifier group =
            Identifier.fromNamespaceAndPath(MOD_ID, "movement");

    @Override
    public void display(@NotNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PlayerMomentumContext context = Services.PLATFORM.getOrCreateStateMachine(mc.player).getContext();
        Vec3 vel = context.getSpeed().multiply(20, 20, 20);
        displayer.addToGroup(group, String.format("[Momentum] H Speed * 20: %.4f", vel.horizontalDistance()));
        displayer.addToGroup(group, String.format("[Momentum] Y Vel * 20: %.4f", vel.y));
        displayer.addToGroup(group, String.format("[Momentum] OnGround: %s", mc.player.onGround()));
        float yRot = mc.player.getYRot(); // 水平朝向 (yaw), -180 到 180
        float xRot = mc.player.getXRot(); // 垂直角度 (pitch), -90 到 90
//        displayer.addToGroup(group, String.format("[Momentum] yRot: %.4f", yRot));
//        displayer.addToGroup(group, String.format("[Momentum] xRot: %.4f", xRot));
        double fallDistance = mc.player.fallDistance;
        displayer.addToGroup(group, String.format("[Momentum] Fall Distance: %.4f", fallDistance));
//        // getWorldInputVec
//        Vec3 inputVec = context.getInputVec();
//        displayer.addToGroup(group, String.format("[Momentum] InputVec: %s", inputVec));
        // getInputAngleToWall
        float inputAngleToWall = context.getInputWallAngle();
        displayer.addToGroup(group, String.format("[Momentum] InputAngleToWall: %.4f", inputAngleToWall));
        // getLookAngleToWall
        float lookAngleToWall = context.getLookWallAngle();
        displayer.addToGroup(group, String.format("[Momentum] LookAngleToWall: %.4f", lookAngleToWall));
        displayer.addToGroup(group, String.format("[Momentum] WallCandidates: %s",
                context.getWallCandidates().stream().map(WallContact::normal).toList()));
        Vec3 wallNormal = context.getWallNormal();
        displayer.addToGroup(group, String.format("[Momentum] ActiveWall: %s %s", context.getActiveWallState(), wallNormal));
        boolean hasLedge = context.isHasLedge();
        displayer.addToGroup(group, String.format("[Momentum] HasLedge: %s", hasLedge));
        Player player = mc.player;
        double jumpLimitSpeed = context.getJumpLimitSpeed();
        displayer.addToGroup(group, String.format("[Momentum] JumpLimitSpeed: %.4f", jumpLimitSpeed * 20));
        //
        float jumpPower = ((LivingEntityAccessor) player).invokeGetJumpPower();
        displayer.addToGroup(group, String.format("[Momentum] JumpPower: %.4f", jumpPower));
    }
}
