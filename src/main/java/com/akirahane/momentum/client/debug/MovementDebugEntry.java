package com.akirahane.momentum.client.debug;

import com.akirahane.momentum.client.MomentumClient;
import com.akirahane.momentum.client.animation.MomentumAnimationController;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.init.InitAttachments;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import team.unnamed.mocha.MochaEngine;

import javax.annotation.Nullable;

import static com.akirahane.momentum.Momentum.MODID;

public class MovementDebugEntry implements DebugScreenEntry {

    private static final Identifier group =
            Identifier.fromNamespaceAndPath(MODID, "movement");

    @Override
    public void display(@NotNull DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        PlayerMovementContext context = mc.player.getData(InitAttachments.MOVEMENT_STATE).getContext();
        Vec3 vel = context.getSpeed().multiply(20, 20, 20);
        displayer.addToGroup(group, String.format("[Momentum] H Speed * 20: %.4f", vel.horizontalDistance()));
        displayer.addToGroup(group, String.format("[Momentum] Y Vel * 20: %.4f", vel.y));
//        displayer.addToGroup(group, String.format("[Momentum] OnGround: %s", mc.player.onGround()));
        float yRot = mc.player.getYRot(); // 水平朝向 (yaw), -180 到 180
        float xRot = mc.player.getXRot(); // 垂直角度 (pitch), -90 到 90
        displayer.addToGroup(group, String.format("[Momentum] yRot: %.4f", yRot));
        displayer.addToGroup(group, String.format("[Momentum] xRot: %.4f", xRot));
//        double fallDistance = mc.player.fallDistance;
//        displayer.addToGroup(group, String.format("[Momentum] Fall Distance: %.4f", fallDistance));
//        // getWorldInputVec
//        Vec3 inputVec = context.getInputVec();
//        displayer.addToGroup(group, String.format("[Momentum] InputVec: %s", inputVec));
        // getInputAngleToWall
        float inputAngleToWall = context.getInputWallAngle();
        displayer.addToGroup(group, String.format("[Momentum] InputAngleToWall: %.4f", inputAngleToWall));
        // getLookAngleToWall
        float lookAngleToWall = context.getLookWallAngle();
        displayer.addToGroup(group, String.format("[Momentum] LookAngleToWall: %.4f", lookAngleToWall));
        boolean hasLedge = context.isHasLedge();
        displayer.addToGroup(group, String.format("[Momentum] HasLedge: %s", hasLedge));
        Player player = mc.player;
        double jumpLimitSpeed = context.getJumpLimitSpeed();
//        displayer.addToGroup(group, String.format("[Momentum] JumpLimitSpeed: %.4f", jumpLimitSpeed * 20));
//        double jumpAcceleration = context.getJumpAcceleration();
//        displayer.addToGroup(group, String.format("[Momentum] JumpAcceleration: %.4f", jumpAcceleration));
//        double safeFallDistance = player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
//        displayer.addToGroup(group, String.format("[Momentum] SafeFallDistance: %.4f", safeFallDistance));
//        MomentumAnimationController controller = (MomentumAnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
//                player, MomentumClient.MOVEMENT_ANIM
//        );
        if (context.getController() != null) {
            MochaEngine<AnimationController> mocha = context.getMocha();
//            // query.ground_speed
//            displayer.addToGroup(group, String.format("[Momentum] GroundSpeed: %.4f", mocha.eval("variable.movement_speed()")));
//            // query.body_x_rotation
//            displayer.addToGroup(group, String.format("[Momentum] BodyXRotation: %.4f", mocha.eval("query.body_x_rotation")));
//            // query.body_y_rotation
//            displayer.addToGroup(group, String.format("[Momentum] BodyYRotation: %.4f", mocha.eval("query.body_y_rotation")));
//            // query.camera_rotation
//            displayer.addToGroup(group, String.format("[Momentum] CameraRotation: %.4f", mocha.eval("query.camera_rotation")));
//            // query.eye_target_x_rotation
//            displayer.addToGroup(group, String.format("[Momentum] EyeTargetXRotation: %.4f", mocha.eval("query.eye_target_x_rotation")));
//            displayer.addToGroup(group, String.format("[Momentum] EyeTargetYRotation: %.4f", mocha.eval("query.eye_target_y_rotation")));
//            // query.head_x_rotation
//            displayer.addToGroup(group, String.format("[Momentum] HeadXRotation: %.4f", mocha.eval("query.head_x_rotation")));
//            displayer.addToGroup(group, String.format("[Momentum] HeadYRotation: %.4f", mocha.eval("query.head_y_rotation")));
            // query.is_swimming
            displayer.addToGroup(group, String.format("[Momentum] IsSwimming: %s", mocha.eval("query.is_swimming")));
            // math.mod(query.head_y_rotation - query.body_y_rotation, 360)
            displayer.addToGroup(group, String.format("[Momentum] HeadYRotation - BodyYRotation: %.4f", mocha.eval("math.mod(query.head_y_rotation - query.body_y_rotation, 360)")));
            boolean isJumping = player.isJumping();
            displayer.addToGroup(group, String.format("[Momentum] IsJumping: %s", isJumping));
            // player.isSprinting()
            displayer.addToGroup(group, String.format("[Momentum] IsSprinting: %s", player.isSprinting()));
        }
    }
}
