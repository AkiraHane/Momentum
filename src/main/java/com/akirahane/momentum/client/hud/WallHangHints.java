package com.akirahane.momentum.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import static com.akirahane.momentum.client.input.LowerCenterKey.LOWER_CENTER;

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
                    .key(OPTIONS.keyUp).slash()
                    .key(OPTIONS.keyLeft).slash()
                    .key(OPTIONS.keyDown).slash()
                    .key(OPTIONS.keyRight)
                    .build();

    public static final HintManager.KeyHint VAULT_IN =
            HintManager.KeyHint.and("state.momentum.vault_in",
                    LOWER_CENTER.get(), OPTIONS.keyJump);

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
}
