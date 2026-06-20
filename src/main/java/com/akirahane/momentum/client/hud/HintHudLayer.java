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

    // 整体缩放（0.75 = 字体变成原来的 75%）
    private static final float SCALE = 1F;

    // 布局参数（在缩放空间内）
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 10;
    private static final int LINE_GAP = 4;
    private static final int KEY_HEIGHT = 16;
    private static final int KEY_PADDING_H = 5;
    private static final int KEY_DESC_GAP = 6;
    private static final int SEPARATOR_GAP = 3;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, HintHudLayer::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!HintManager.isVisible()) return;
        if (HintManager.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

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
        for (KeyHint hint : hints) {
            int y = startY + i * (KEY_HEIGHT + LINE_GAP);
            renderHintLine(graphics, font, hint, PADDING_X, y);
            i++;
        }

        graphics.pose().popMatrix();
    }

    private static void renderHintLine(GuiGraphicsExtractor graphics, Font font,
                                       KeyHint hint, int x, int y) {
        int currentX = x;
        int textY = y + (KEY_HEIGHT - font.lineHeight) / 2 + 1;

        for (KeyHint.Element element : hint.elements()) {
            if (element instanceof KeyHint.KeyElement(KeyMapping key)) {
                String keyName = key.getTranslatedKeyMessage().getString();
                int keyTextWidth = font.width(keyName);
                int keyBgWidth = Math.max(KEY_HEIGHT, keyTextWidth + KEY_PADDING_H * 2);

                graphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        KEY_BUTTON_SPRITE,
                        currentX, y, keyBgWidth, KEY_HEIGHT
                );

                graphics.text(font, keyName,
                        currentX + (keyBgWidth - keyTextWidth) / 2,
                        textY, TEXT_COLOR, true);

                currentX += keyBgWidth;
            } else if (element instanceof KeyHint.TextElement(String text)) {
                currentX += SEPARATOR_GAP;
                graphics.text(font, text, currentX, textY, TEXT_COLOR, true);
                currentX += font.width(text) + SEPARATOR_GAP;
            }
        }

        // 描述文字
        currentX += KEY_DESC_GAP;
        graphics.text(font, hint.description(), currentX, textY, TEXT_COLOR, true);
    }

}