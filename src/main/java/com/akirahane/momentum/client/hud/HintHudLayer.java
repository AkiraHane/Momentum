package com.akirahane.momentum.client.hud;

import com.akirahane.momentum.Momentum;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class HintHudLayer {

    public static final Identifier LAYER_ID = Identifier.fromNamespaceAndPath(
            Momentum.MODID, "hint_overlay");

    private static final Identifier KEY_BUTTON_SPRITE = Identifier.fromNamespaceAndPath(
            Momentum.MODID, "widget/button16");

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

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, HintHudLayer::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
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
        for (HintManager.KeyHint hint : hints) {
            int y = startY + i * (KEY_HEIGHT + LINE_GAP);
            renderHintLine(graphics, font, hint, PADDING_X, y, alpha);
            i++;
        }

        graphics.pose().popMatrix();
    }

    private static void renderHintLine(GuiGraphicsExtractor graphics, Font font,
                                       HintManager.KeyHint hint, int x, int y, float alpha) {
        int currentX = x;
        int textY = y + (KEY_HEIGHT - font.lineHeight) / 2 + 1;
        int alphaByte = (int) (alpha * 255) & 0xFF;
        int normalTextColor = (alphaByte << 24) | RGB_NORMAL;

        for (HintManager.Element element : hint.elements()) {
            if (element instanceof HintManager.KeyElement(net.minecraft.client.KeyMapping key)) {
                String keyName = key.getTranslatedKeyMessage().getString();
                int keyTextWidth = font.width(keyName);
                int keyBgWidth = Math.max(KEY_HEIGHT, keyTextWidth + KEY_PADDING_H * 2);

                // 检测按键是否按下，按下时高亮
                boolean pressed = isKeyDown(key);
                float bgAlpha = pressed ? Math.min(1.0f, alpha * 1.5f) : alpha;
                int textColor = pressed
                        ? ((alphaByte << 24) | RGB_PRESSED)
                        : normalTextColor;

                // 按键背景
                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        KEY_BUTTON_SPRITE,
                        currentX, y, keyBgWidth, KEY_HEIGHT,
                        bgAlpha
                );

                // 按键文字（按下时变金黄色）
                graphics.text(font, keyName,
                        currentX + (keyBgWidth - keyTextWidth) / 2,
                        textY, textColor, true);

                currentX += keyBgWidth;
            } else if (element instanceof HintManager.TextElement(String text)) {
                currentX += SEPARATOR_GAP;
                graphics.text(font, text, currentX, textY, normalTextColor, true);
                currentX += font.width(text) + SEPARATOR_GAP;
            }
        }

        // 描述文字
        currentX += KEY_DESC_GAP;
        graphics.text(font, hint.description(), currentX, textY, normalTextColor, true);
    }

    private static boolean isKeyDown(net.minecraft.client.KeyMapping mapping) {
        try {
            return mapping.isDown();
        } catch (Exception e) {
            return false;
        }
    }
}