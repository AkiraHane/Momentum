package com.akirahane.momentum.fabric.client.hud;

import com.akirahane.momentum.MomentumConstants;
import com.akirahane.momentum.fabric.client.FabricKeyMappings;
import com.akirahane.momentum.platform.client.MovementHint;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class FabricHintHud {
    private static final Identifier LAYER_ID = id("hint_overlay");
    private static final Identifier KEY_BUTTON_SPRITE = id("widget/button16");
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 10;
    private static final int LINE_GAP = 4;
    private static final int KEY_HEIGHT = 16;
    private static final int KEY_PADDING = 5;
    private static final int DESCRIPTION_GAP = 6;
    private static final int SEPARATOR_GAP = 3;
    private static final int RGB_NORMAL = 0x00FFFFFF;
    private static final int RGB_PRESSED = 0x00434A5F;

    private FabricHintHud() {
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR, LAYER_ID, FabricHintHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!FabricHintManager.isVisible() || FabricHintManager.hints().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        float alpha = FabricHintManager.alpha(
                deltaTracker.getGameTimeDeltaPartialTick(false));
        if (alpha < 0.01F) {
            return;
        }

        List<MovementHint> hints = FabricHintManager.hints();
        int totalHeight = hints.size() * KEY_HEIGHT
                + Math.max(0, hints.size() - 1) * LINE_GAP;
        int startY = graphics.guiHeight() - PADDING_Y - totalHeight;
        int row = 0;
        for (int index = hints.size() - 1; index >= 0; index--) {
            renderLine(graphics, minecraft.font, resolve(hints.get(index)),
                    PADDING_X, startY + row * (KEY_HEIGHT + LINE_GAP), alpha);
            row++;
        }
    }

    private static void renderLine(
            GuiGraphicsExtractor graphics, Font font, HintLine hint, int x, int y, float alpha) {
        int currentX = x;
        int textY = y + (KEY_HEIGHT - font.lineHeight) / 2 + 1;
        int alphaByte = (int) (alpha * 255.0F) & 0xFF;
        int normalColor = alphaByte << 24 | RGB_NORMAL;

        for (HintElement element : hint.elements()) {
            if (element instanceof KeyElement(KeyMapping key)) {
                String keyName = key.getTranslatedKeyMessage().getString();
                int keyTextWidth = font.width(keyName);
                int backgroundWidth = Math.max(KEY_HEIGHT, keyTextWidth + KEY_PADDING * 2);
                boolean pressed = key.isDown();
                float backgroundAlpha = pressed ? Math.min(1.0F, alpha * 1.5F) : alpha;
                int textColor = pressed ? alphaByte << 24 | RGB_PRESSED : normalColor;

                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, KEY_BUTTON_SPRITE,
                        currentX, y, backgroundWidth, KEY_HEIGHT, backgroundAlpha);
                graphics.text(font, keyName,
                        currentX + (backgroundWidth - keyTextWidth) / 2,
                        textY, textColor, true);
                currentX += backgroundWidth;
            } else if (element instanceof TextElement(Component text)) {
                currentX += SEPARATOR_GAP;
                graphics.text(font, text, currentX, textY, normalColor, true);
                currentX += font.width(text) + SEPARATOR_GAP;
            }
        }

        currentX += DESCRIPTION_GAP;
        graphics.text(font, hint.description(), currentX, textY, normalColor, true);
    }

    private static HintLine resolve(MovementHint hint) {
        var options = Minecraft.getInstance().options;
        return switch (hint) {
            case BREAK_FALL_READY -> single(FabricKeyMappings.LOWER_CENTER, "state.momentum.break_fall_ready");
            case BREAK_FALL_READY_EGG -> single(FabricKeyMappings.LOWER_CENTER, "state.momentum.break_fall_ready_egg");
            case PRONE -> single(FabricKeyMappings.LOWER_CENTER, "state.momentum.prone");
            case SLIDE -> single(FabricKeyMappings.LOWER_CENTER, "state.momentum.slide");
            case DODGE_DIR_DOUBLE -> prefixed("state.momentum.dodge", "hint.momentum.double_click",
                    options.keyUp, options.keyLeft, options.keyDown, options.keyRight);
            case DODGE_SPRINT_CLICK -> single(options.keySprint, "state.momentum.dodge");
            case DODGE_SPRINT_DOUBLE -> prefixed("state.momentum.dodge", "hint.momentum.double_click",
                    options.keySprint);
            case VAULT_IN -> joined("state.momentum.vault_in", "+",
                    FabricKeyMappings.LOWER_CENTER, options.keyJump);
            case VAULT_IN_STAND -> joined("state.momentum.vault_in", "+",
                    options.keyUp, FabricKeyMappings.LOWER_CENTER);
            case VAULT_UP -> single(options.keyJump, "state.momentum.vault_up");
            case WALL_CLIMB -> single(options.keyJump, "state.momentum.wall_climb");
            case WALL_SLIDE -> single(options.keyJump, "state.momentum.wall_slide");
            case WALL_HANG -> single(options.keyShift, "state.momentum.wall_hang");
            case WALL_KICK -> prefixed("state.momentum.wall_kick", "hint.momentum.click", options.keyJump);
            case POWER_JUMP_READY -> powerJumpReady(options.keyShift, options.keyJump);
            case POWER_JUMP -> single(options.keyJump, "state.momentum.power_jump");
            case WALL_RUN -> joined("state.momentum.wall_run", "+", options.keyUp, options.keyJump);
            case WALL_RUN_HOLD -> single(options.keyUp, "state.momentum.wall_run");
            case TOGGLE_HINT -> single(FabricKeyMappings.TOGGLE_HINTS, "hint.momentum.hit_toggle");
            case ORIGINAL_STATE -> single(FabricKeyMappings.TOGGLE_MOMENTUM, "hint.momentum.momentum_toggle");
            case CLIMB_ACCELERATION -> single(options.keySprint, "hint.momentum.climb_acceleration");
            case SWIM -> joined("state.momentum.swim", "+", options.keySprint, options.keyUp);
            case SWIM_HOLD -> single(options.keyUp, "state.momentum.swim");
            case SWIM_ACTIVE -> joined("state.momentum.swim", "+", FabricKeyMappings.LOWER_CENTER, options.keyUp);
            case PUSH -> single(options.keySprint, "hint.momentum.push");
            case PUSH_UP -> joined("hint.momentum.push", "+", options.keyUp, options.keySprint);
            case SLOW_FALL -> prefixed("hint.momentum.slow_fall", "hint.momentum.hold", options.keyJump);
            case AIR_JUMP -> prefixed("hint.momentum.air_jump", "hint.momentum.click", options.keyJump);
            case WATER_RUN -> prefixed("hint.momentum.water_run", "hint.momentum.click", options.keyJump);
        };
    }

    private static HintLine single(KeyMapping key, String description) {
        return new HintLine(List.of(new KeyElement(key)), Component.translatable(description));
    }

    private static HintLine joined(String description, String separator, KeyMapping... keys) {
        List<HintElement> elements = new ArrayList<>();
        for (int index = 0; index < keys.length; index++) {
            if (index > 0) {
                elements.add(new TextElement(Component.literal(separator)));
            }
            elements.add(new KeyElement(keys[index]));
        }
        return new HintLine(elements, Component.translatable(description));
    }

    private static HintLine prefixed(String description, String prefix, KeyMapping... keys) {
        List<HintElement> elements = new ArrayList<>();
        elements.add(new TextElement(Component.translatable(prefix)));
        for (KeyMapping key : keys) {
            elements.add(new KeyElement(key));
        }
        return new HintLine(elements, Component.translatable(description));
    }

    private static HintLine powerJumpReady(KeyMapping shift, KeyMapping jump) {
        return new HintLine(List.of(
                new TextElement(Component.translatable("hint.momentum.release")),
                new KeyElement(shift),
                new TextElement(Component.literal("+")),
                new KeyElement(jump)), Component.translatable("state.momentum.power_jump"));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, path);
    }

    private sealed interface HintElement permits KeyElement, TextElement {
    }

    private record KeyElement(KeyMapping key) implements HintElement {
    }

    private record TextElement(Component text) implements HintElement {
    }

    private record HintLine(List<HintElement> elements, Component description) {
    }
}
