package com.akirahane.momentum.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class WallHangHints {
    private static final Options OPTIONS = Minecraft.getInstance().options;

    public static final HintManager.KeyHint JUMP =
            HintManager.KeyHint.single(OPTIONS.keyJump, "hint.momentum.jump");

    public static final HintManager.KeyHint DASH =
            HintManager.KeyHint.and("hint.momentum.dash",
                    OPTIONS.keySprint, OPTIONS.keyJump);

    public static final HintManager.KeyHint LOOK =
            HintManager.KeyHint.or("hint.momentum.look",
                    OPTIONS.keyUp, OPTIONS.keyDown);

    public static final HintManager.KeyHint MOVE =
            HintManager.KeyHint.builder("hint.momentum.move")
                    .key(OPTIONS.keySprint).plus()
                    .key(OPTIONS.keyUp).slash()
                    .key(OPTIONS.keyLeft).slash()
                    .key(OPTIONS.keyDown).slash()
                    .key(OPTIONS.keyRight)
                    .build();

    public static final HintManager.KeyHint BREAK_FALL_READY =
            HintManager.KeyHint.builder("hint.momentum.break_fall_ready")
                    .key(OPTIONS.keySprint)
                    .slash()
                    .key(OPTIONS.keyUp)
                    .slash()
                    .key(OPTIONS.keyLeft)
                    .slash()
                    .key(OPTIONS.keyDown)
                    .slash()
                    .key(OPTIONS.keyRight)
                    .build();
}
