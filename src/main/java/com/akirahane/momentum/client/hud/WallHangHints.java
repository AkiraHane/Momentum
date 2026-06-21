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

    public static final HintManager.KeyHint PRONE =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.prone");

    public static final HintManager.KeyHint SLIDE =
            HintManager.KeyHint.single(LOWER_CENTER.get(), "state.momentum.slide");

    public static final HintManager.KeyHint DODGE =
            HintManager.KeyHint.builder("state.momentum.dodge")
                    .key(OPTIONS.keySprint).plus()
                    .translatable("hint.momentum.double_click")
                    .key(OPTIONS.keyUp)
                    .key(OPTIONS.keyLeft)
                    .key(OPTIONS.keyDown)
                    .key(OPTIONS.keyRight)
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
            HintManager.KeyHint.single(CHANGE_MOMENTUM_KEY_MAPPING.get(), "state.momentum.original");
}
