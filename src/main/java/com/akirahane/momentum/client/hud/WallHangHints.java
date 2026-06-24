package com.akirahane.momentum.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import static com.akirahane.momentum.client.input.ChangeMomentumKey.CHANGE_MOMENTUM_KEY_MAPPING;
import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;
import static com.akirahane.momentum.client.input.ToggleHintKey.TOGGLE_HINT_KEY_MAPPING;

public class WallHangHints {
    private static final Options OPTIONS = Minecraft.getInstance().options;

    public static final HintManager.KeyHint BREAK_FALL_READY =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.break_fall_ready");

    public static final HintManager.KeyHint BREAK_FALL_READY_EGG =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.break_fall_ready_egg");

    public static final HintManager.KeyHint PRONE =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.prone");

    public static final HintManager.KeyHint SLIDE =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.slide");

    public static final HintManager.KeyHint DODGE_DOUBLE =
            HintManager.KeyHint.builder("state.momentum.dodge")
                    .key(OPTIONS.keyUp)
                    .key(OPTIONS.keyLeft)
                    .key(OPTIONS.keyDown)
                    .key(OPTIONS.keyRight).plus()
                    .translatable("hint.momentum.double_click")
                    .key(OPTIONS.keySprint)
                    .build();

    public static final HintManager.KeyHint DODGE =
            HintManager.KeyHint.builder("state.momentum.dodge")
                    .key(OPTIONS.keyUp)
                    .key(OPTIONS.keyLeft)
                    .key(OPTIONS.keyDown)
                    .key(OPTIONS.keyRight).plus()
                    .translatable("hint.momentum.click")
                    .key(OPTIONS.keySprint)
                    .build();

    public static final HintManager.KeyHint VAULT_IN =
            HintManager.KeyHint.and("state.momentum.vault_in",
                    LOWER_CENTER.get(), OPTIONS.keyJump);

    public static final HintManager.KeyHint VAULT_IN_STAND =
            HintManager.KeyHint.and("state.momentum.vault_in",
                    OPTIONS.keyUp, LOWER_CENTER.get());

    public static final HintManager.KeyHint VAULT_UP =
            HintManager.KeyHint.single(OPTIONS.keyJump, "state.momentum.vault_up");

    public static final HintManager.KeyHint WALL_CLIMB =
            HintManager.KeyHint.single(OPTIONS.keyJump, "state.momentum.wall_climb");

    public static final HintManager.KeyHint WALL_SLIDE =
            HintManager.KeyHint.single(OPTIONS.keyJump, "state.momentum.wall_slide");

    public static final HintManager.KeyHint WALL_HANG =
            HintManager.KeyHint.single(OPTIONS.keyShift, "state.momentum.wall_hang");

    public static final HintManager.KeyHint WALL_KICK =
            HintManager.KeyHint.builder("state.momentum.wall_kick")
                    .translatable("hint.momentum.click")
                    .key(OPTIONS.keyJump)
                    .build();

    public static final HintManager.KeyHint WALL_RUN =
            HintManager.KeyHint.and("state.momentum.wall_run", OPTIONS.keyUp, OPTIONS.keyJump);

    public static final HintManager.KeyHint WALL_RUN_HOLD =
            HintManager.KeyHint.single(OPTIONS.keyUp, "state.momentum.wall_run");

    // 开关提示
    public static final HintManager.KeyHint TOGGLE_HINT =
            HintManager.KeyHint.single(TOGGLE_HINT_KEY_MAPPING.get(), "hint.momentum.hit_toggle");

    // 进入原版状态
    public static final HintManager.KeyHint ORIGINAL_STATE =
            HintManager.KeyHint.single(CHANGE_MOMENTUM_KEY_MAPPING.get(), "hint.momentum.momentum_toggle");

    // 攀爬加速
    public static final HintManager.KeyHint CLIMB_ACCELERATION =
            HintManager.KeyHint.single(OPTIONS.keySprint, "hint.momentum.climb_acceleration");

    // 游泳
    public static final HintManager.KeyHint SWIM =
            HintManager.KeyHint.and("state.momentum.swim", OPTIONS.keySprint, OPTIONS.keyUp);

    // 游泳维持
    public static final HintManager.KeyHint SWIM_HOLD =
            HintManager.KeyHint.single(OPTIONS.keyUp, "state.momentum.swim");

    // 主动游泳
    public static final HintManager.KeyHint SWIM_ACTIVE =
            HintManager.KeyHint.and("state.momentum.swim", LOWER_CENTER.get(), OPTIONS.keyUp);

    // 推进
    public static final HintManager.KeyHint PUSH =
            HintManager.KeyHint.single(OPTIONS.keySprint, "hint.momentum.push");
    public static final HintManager.KeyHint PUSH_UP =
            HintManager.KeyHint.and(
                    "hint.momentum.push",
                    OPTIONS.keyUp,
                    OPTIONS.keySprint
            );

    // 缓降
    public static final HintManager.KeyHint SLOW_FALL =
            HintManager.KeyHint.builder("hint.momentum.slow_fall")
                    .translatable("hint.momentum.hold")
                    .key(OPTIONS.keyJump)
                    .build();
    // 二段跳
    public static final HintManager.KeyHint AIR_JUMP =
            HintManager.KeyHint.builder("hint.momentum.air_jump")
                    .translatable("hint.momentum.click")
                    .key(OPTIONS.keyJump)
                    .build();

    // 水上跑
    public static final HintManager.KeyHint WATER_RUN =
            HintManager.KeyHint.builder("hint.momentum.water_run")
                    .translatable("hint.momentum.click")
                    .key(OPTIONS.keyJump)
                    .build();
}
