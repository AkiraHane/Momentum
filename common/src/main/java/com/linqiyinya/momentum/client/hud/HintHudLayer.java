package com.linqiyinya.momentum.client.hud;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.client.input.ChangeMomentumKey;
import com.linqiyinya.momentum.client.input.ToggleHintKey;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class HintHudLayer {

    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(
            Constants.MOD_ID, "hint_overlay");

    private static final Identifier KEY_BUTTON_SPRITE = Identifier.fromNamespaceAndPath(
            Constants.MOD_ID, "widget/button16");

    // 整体缩放
    private static final float SCALE = 1F;

    // 布局参数
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 10;
    private static final int LINE_GAP = 4;
    private static final int KEY_HEIGHT = 16;
    private static final int KEY_PADDING_H = 5;
    private static final int KEY_DESC_GAP = 6;
    private static final int SEPARATOR_GAP = 3;

    // 颜色
    private static final int RGB_NORMAL = 0x00FFFFFF;        // 普通白色
    private static final int RGB_PRESSED = 0x00434A5F;        // 按下时的灰色

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        render(graphics, delta, null);
    }

    // Fabric 的独立组合键单独绘制；NeoForge 的按键名称已包含修饰键。
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker delta, KeyMapping functionModifier) {
        if (!HintManager.isVisible()) return;
        if (HintManager.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        float alpha = HintManager.getAlpha(partialTick);
        if (alpha < 0.01f) return;

        Font font = mc.font;

        // 应用整体缩放
        graphics.pose().pushMatrix();
        graphics.pose().scale(SCALE, SCALE);

        // 缩放后的等效屏幕尺寸
        int scaledScreenHeight = (int) (graphics.guiHeight() / SCALE);

        var hints = HintManager.getAll();
        int totalHeight = hints.size() * KEY_HEIGHT + Math.max(0, hints.size() - 1) * LINE_GAP;
        int startY = scaledScreenHeight - PADDING_Y - totalHeight;

        int i = 0;
        for (int index = hints.size() - 1; index >= 0; index--) {
            int y = startY + i * (KEY_HEIGHT + LINE_GAP);
            renderHintLine(graphics, font, hints.get(index), PADDING_X, y, alpha, functionModifier);
            i++;
        }

        graphics.pose().popMatrix();
    }

    private static void renderHintLine(GuiGraphicsExtractor graphics, Font font,
                                       HintManager.KeyHint hint, int x, int y, float alpha, KeyMapping functionModifier) {
        int currentX = x;
        int textY = y + (KEY_HEIGHT - font.lineHeight) / 2 + 1;
        int alphaByte = (int) (alpha * 255) & 0xFF;
        int normalTextColor = (alphaByte << 24) | RGB_NORMAL;

        for (HintManager.Element element : hint.elements()) {
            if (element instanceof HintManager.KeyElement(KeyMapping key)) {
                // 每帧读取绑定，使改绑或清除组合键后提示立即跟随变化。
                if (functionModifier != null && !functionModifier.isUnbound()
                        && (key.getName().equals(ChangeMomentumKey.KEY_NAME)
                        || key.getName().equals(ToggleHintKey.KEY_NAME))) {
                    currentX = renderKey(graphics, font, functionModifier, currentX, y, alpha);
                    currentX += SEPARATOR_GAP;
                    graphics.text(font, "+", currentX, textY, normalTextColor, true);
                    currentX += font.width("+") + SEPARATOR_GAP;
                }
                currentX = renderKey(graphics, font, key, currentX, y, alpha);
            } else if (element instanceof HintManager.TextElement(Component text)) {
                currentX += SEPARATOR_GAP;
                graphics.text(font, text, currentX, textY, normalTextColor, true);
                currentX += font.width(text) + SEPARATOR_GAP;
            }
        }

        // 描述文字
        currentX += KEY_DESC_GAP;
        graphics.text(font, hint.description(), currentX, textY, normalTextColor, true);
    }

    private static int renderKey(GuiGraphicsExtractor graphics, Font font, KeyMapping key,
                                 int x, int y, float alpha) {
        String keyName = key.getTranslatedKeyMessage().getString();
        int keyTextWidth = font.width(keyName);
        int keyBgWidth = Math.max(KEY_HEIGHT, keyTextWidth + KEY_PADDING_H * 2);
        int textY = y + (KEY_HEIGHT - font.lineHeight) / 2 + 1;
        int alphaByte = (int) (alpha * 255) & 0xFF;

        boolean pressed = isKeyDown(key);
        float bgAlpha = pressed ? Math.min(1.0f, alpha * 1.5f) : alpha;
        int textColor = (alphaByte << 24) | (pressed ? RGB_PRESSED : RGB_NORMAL);

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                KEY_BUTTON_SPRITE,
                x, y, keyBgWidth, KEY_HEIGHT,
                bgAlpha
        );
        graphics.text(font, keyName,
                x + (keyBgWidth - keyTextWidth) / 2,
                textY, textColor, true);
        return x + keyBgWidth;
    }

    private static boolean isKeyDown(KeyMapping mapping) {
        try {
            return mapping.isDown();
        } catch (Exception e) {
            return false;
        }
    }
}
