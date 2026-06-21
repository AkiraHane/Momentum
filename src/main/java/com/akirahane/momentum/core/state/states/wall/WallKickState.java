package com.akirahane.momentum.core.state.states.wall;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.state.StateType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

public class WallKickState extends BaseState {
    // 跳跃
    public static String WALL_JUMP_LEFT = "wall_jump_left";
    public static String WALL_JUMP_RIGHT = "wall_jump_right";

    public static boolean canWallKick(Player player, PlayerMovementContext context) {
        return !Vec3.ZERO.equals(context.getWallNormal()) &&
                context.getInputVec().horizontalDistance() > 0.01 && Mth.abs(context.getInputWallAngle()) >= 100 &&
                checkKey(player, context);
    }

    public static boolean checkKey(Player player, PlayerMovementContext context) {
        HintManager.add(WallHangHints.WALL_KICK);
        return context.getInputBuffer()[context.getInputBufferIndex()].contains(JUMP);
    }


    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        if (context.isLeftFootJump()) {
            playStateAnimation(player, WALL_JUMP_LEFT, context);
        } else {
            playStateAnimation(player, WALL_JUMP_RIGHT, context);
        }
        context.setLeftFootJump(!context.isLeftFootJump());
        player.addDeltaMovement(
                new Vec3(
                        context.getInputVec().x * 0.3,
                        0.6,
                        context.getInputVec().z * 0.3
                )
        );
        context.playWallSound(player, FALL, 0.15F, 1);
        player.playSound(
                SoundEvents.ARROW_SHOOT,
                0.5F,
                1.0F + player.getRandom().nextFloat() * 0.4F - 0.2F  // 0.8 ~ 1.2 随机音高
        );
    }

    @Override
    public StateType getStateType() {
        return StateType.WALL_KICK;
    }
}
