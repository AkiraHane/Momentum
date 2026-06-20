package com.akirahane.momentum.core.state.states.ground;

import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.KeyHint;
import com.akirahane.momentum.core.state.StateType;
import com.akirahane.momentum.core.state.BaseState;
import com.akirahane.momentum.core.context.PlayerMovementContext;
import com.akirahane.momentum.core.state.states.air.AirborneState;
import com.akirahane.momentum.core.state.states.special.DodgeState;
import com.akirahane.momentum.core.state.states.wall.WallClimbState;
import com.akirahane.momentum.core.state.states.wall.WallRunState;
import com.akirahane.momentum.core.state.states.wall.WallSlideState;
import com.akirahane.momentum.core.state.states.water.SwimState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;

import static com.akirahane.momentum.core.state.states.OriginalState.canOriginal;

public class WalkState extends BaseState {
    public static boolean canWalk(Player player, PlayerMovementContext context) {
        return player.onGround();
    }

    @Override
    public void onEnter(Player player, PlayerMovementContext context) {
        super.onEnter(player, context);
        Options options = Minecraft.getInstance().options;
        HintManager.add("jump", KeyHint.single(options.keyJump, "hint.momentum.jump"));

// 简单：多键统一用 +
        HintManager.add("dash", KeyHint.and("hint.momentum.dash",
                options.keySprint, options.keyJump));
// 显示：[Ctrl] + [Space] 冲刺

// 简单：多键统一用 /
        HintManager.add("look", KeyHint.or("hint.momentum.look",
                options.keyUp, options.keyDown));
// 显示：[W] / [S] 视角

// 混合：Ctrl + W/A/S/D
        HintManager.add("move", KeyHint.builder("hint.momentum.move")
                .key(options.keySprint).plus()
                .key(options.keyUp).slash()
                .key(options.keyLeft).slash()
                .key(options.keyDown).slash()
                .key(options.keyRight).key(options.keyRight)
                .build());
// 显示：[Ctrl] + [W] / [A] / [S] / [D] 移动
    }

    @Override
    public BaseState evaluate(Player player, PlayerMovementContext context) {
        if (canOriginal(player, context)) {
            return StateType.ORIGINAL.getState();
        }
        if (DodgeState.canDodge(player, context)) {
            return StateType.DODGE.getState();
        }
        if (SwimState.canSwim(player, context)) {
            return StateType.SWIM.getState();
        }
        if (WallClimbState.canWallClimb(player, context)) {
            return StateType.WALL_CLIMB.getState();
        }
        if (WallSlideState.canWallSlide(player, context)) {
            return StateType.WALL_SLIDE.getState();
        }
        if (AirborneState.canAirborne(player, context)) {
            return StateType.AIRBORNE.getState();
        }
        if (SlideState.canSlide(player, context)) {
            return StateType.SLIDE.getState();
        }
        if (ProneState.canProne(player, context)) {
            return StateType.PRONE.getState();
        }
        return StateType.WALK.getState();
    }

    @Override
    public StateType getStateType() {
        return StateType.WALK;
    }
}
