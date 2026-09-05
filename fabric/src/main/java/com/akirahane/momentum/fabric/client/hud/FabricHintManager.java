package com.akirahane.momentum.fabric.client.hud;

import com.akirahane.momentum.fabric.config.FabricClientConfig;
import com.akirahane.momentum.platform.client.MovementHint;
import com.akirahane.momentum.platform.config.MomentumClientConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FabricHintManager {
    private static final List<MovementHint> HINTS = new ArrayList<>();
    private static boolean visible;
    private static int contentChangeTick;
    private static int stillTick;
    private static int globalTick;
    private static float currentAlpha;
    private static float previousAlpha;

    private FabricHintManager() {
    }

    public static void initialize() {
        visible = MomentumClientConfig.ENABLE_KEY_HINTS.getAsBoolean();
    }

    public static void add(MovementHint hint) {
        if (!HINTS.contains(hint)) {
            HINTS.add(hint);
        }
    }

    public static boolean contains(MovementHint hint) {
        return HINTS.contains(hint);
    }

    public static void clear() {
        if (!HINTS.isEmpty()) {
            HINTS.clear();
        }
    }

    public static List<MovementHint> hints() {
        return Collections.unmodifiableList(HINTS);
    }

    public static boolean isVisible() {
        return visible;
    }

    public static void toggleVisible() {
        visible = !visible;
        MomentumClientConfig.ENABLE_KEY_HINTS.updateFromPlatform(visible);
        FabricClientConfig.save();
        if (visible) {
            forceShow();
        }
    }

    public static void clientTick(Player player) {
        globalTick++;
        previousAlpha = currentAlpha;

        Vec3 movement = player.getDeltaMovement();
        boolean moving = movement.horizontalDistanceSqr() > MomentumClientConfig.MOVE_THRESHOLD_SQR.get()
                || Math.abs(movement.y) > 0.1;
        stillTick = moving ? 0 : stillTick + 1;

        float targetAlpha = targetAlpha(moving);
        float speed = currentAlpha < targetAlpha
                ? MomentumClientConfig.FADE_IN_SPEED.get().floatValue()
                : MomentumClientConfig.FADE_OUT_SPEED.get().floatValue();
        currentAlpha += (targetAlpha - currentAlpha) * speed;
        if (Math.abs(currentAlpha - targetAlpha) < 0.001F) {
            currentAlpha = targetAlpha;
        }
    }

    public static float alpha(float partialTick) {
        return Mth.lerp(partialTick, previousAlpha, currentAlpha);
    }

    private static float targetAlpha(boolean moving) {
        if (!visible) {
            return 0.0F;
        }
        if (globalTick - contentChangeTick < MomentumClientConfig.FRESH_DURATION.getAsInt()) {
            return MomentumClientConfig.MAX_ALPHA.get().floatValue();
        }
        if (stillTick >= MomentumClientConfig.IDLE_DELAY.getAsInt()) {
            return MomentumClientConfig.MAX_ALPHA.get().floatValue();
        }
        return MomentumClientConfig.MIN_ALPHA_WHEN_MOVING.get().floatValue();
    }

    private static void forceShow() {
        contentChangeTick = globalTick;
        currentAlpha = MomentumClientConfig.MAX_ALPHA.get().floatValue();
        previousAlpha = currentAlpha;
    }
}
