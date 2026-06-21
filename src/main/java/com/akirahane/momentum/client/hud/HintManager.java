package com.akirahane.momentum.client.hud;

import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static com.akirahane.momentum.client.config.ClientConfig.*;

public class HintManager {

    // ========== 数据结构 ==========

    public sealed interface Element {
    }

    public record KeyElement(KeyMapping key) implements Element {
    }

    public record TextElement(Component text) implements Element {
    }

    public record KeyHint(List<Element> elements, Component description) {

        public static KeyHint single(KeyMapping key, String translationKey) {
            return new KeyHint(List.of(new KeyElement(key)), Component.translatable(translationKey));
        }

        public static KeyHint and(String translationKey, KeyMapping... keys) {
            return joinKeys(translationKey, "+", keys);
        }

        public static KeyHint or(String translationKey, KeyMapping... keys) {
            return joinKeys(translationKey, "/", keys);
        }

        private static KeyHint joinKeys(String translationKey, String sep, KeyMapping... keys) {
            List<Element> list = new ArrayList<>();
            Component sepComponent = Component.literal(sep);
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) list.add(new TextElement(sepComponent));
                list.add(new KeyElement(keys[i]));
            }
            return new KeyHint(list, Component.translatable(translationKey));
        }

        public static Builder builder(String translationKey) {
            return new Builder(Component.translatable(translationKey));
        }

        public static class Builder {
            private final List<Element> elements = new ArrayList<>();
            private final Component description;

            public Builder(Component description) {
                this.description = description;
            }

            public Builder key(KeyMapping key) {
                elements.add(new KeyElement(key));
                return this;
            }

            public Builder plus() {
                elements.add(new TextElement(Component.literal("+")));
                return this;
            }

            public Builder slash() {
                elements.add(new TextElement(Component.literal("/")));
                return this;
            }

            public Builder text(String text) {
                elements.add(new TextElement(Component.literal(text)));
                return this;
            }

            /**
             * 翻译键（多语言）
             */
            public Builder translatable(String translationKey) {
                elements.add(new TextElement(Component.translatable(translationKey)));
                return this;
            }

            /**
             * 直接传 Component
             */
            public Builder component(Component text) {
                elements.add(new TextElement(text));
                return this;
            }

            public KeyHint build() {
                return new KeyHint(List.copyOf(elements), description);
            }
        }
    }

    // ========== 状态管理 ==========

    private static final List<KeyHint> HINTS = new ArrayList<>();
    @Getter
    private static boolean visible = false;

    // 各种 tick 计数
    private static int contentChangeTick = 0;    // 提示内容上次变化时间
    private static int stillTick = 0;            // 玩家停止移动持续时间

    // alpha 状态
    private static float currentAlpha = 0f;
    private static float prevAlpha = 0f;
    private static int globalTick = 0;

    // ========== 公开 API ==========

    public static void add(KeyHint hint) {
        if (!HINTS.contains(hint)) {
            HINTS.add(hint);
        }
    }

    // 判断是否有
    public static boolean contains(KeyHint hint) {
        return HINTS.contains(hint);
    }

    public static void remove(KeyHint hint) {
        HINTS.remove(hint);
    }

    public static void clear() {
        HINTS.clear();
    }

    public static List<KeyHint> getAll() {
        return HINTS;
    }

    public static boolean isEmpty() {
        return HINTS.isEmpty();
    }

    public static void setVisible(boolean visible) {
        HintManager.visible = visible;
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    // ========== Tick 更新 ==========

    public static void clientTick(Player player) {
        globalTick++;
        prevAlpha = currentAlpha;

        // 检测玩家移动状态
        Vec3 movement = player.getDeltaMovement();
        boolean moving = movement.horizontalDistanceSqr() > MOVE_THRESHOLD_SQR.get()
                || Math.abs(movement.y) > 0.1;

        if (moving) {
            stillTick = 0;
        } else {
            stillTick++;
        }

        // 3. 计算目标 alpha
        float targetAlpha = computeTargetAlpha(moving);

        // 4. 平滑过渡（不对称速度）
        float speed = (currentAlpha < targetAlpha) ? FADE_IN_SPEED.get().floatValue() : FADE_OUT_SPEED.get().floatValue();
        currentAlpha = lerpEased(currentAlpha, targetAlpha, speed);

        // 防止浮点抖动
        if (Math.abs(currentAlpha - targetAlpha) < 0.001f) {
            currentAlpha = targetAlpha;
        }
    }

    private static float computeTargetAlpha(boolean moving) {
        if (!visible) return 0f;

        int sinceChange = globalTick - contentChangeTick;

        // 优先级 1：强制显示
        if (sinceChange < FRESH_DURATION.get().floatValue()) {
            return MAX_ALPHA.get().floatValue();
        }

        // 优先级 2：玩家静止超过阈值，显示提示
        if (stillTick >= IDLE_DELAY.get().floatValue()) {
            return MAX_ALPHA.get().floatValue();
        }

        // 优先级 3：玩家在移动，降到最低可见度
        if (moving) {
            return MIN_ALPHA_WHEN_MOVING.get().floatValue();
        }

        // 默认：中等透明度
        return MIN_ALPHA_WHEN_MOVING.get().floatValue();
    }

    /**
     * 缓动插值（EaseOutCubic 风格）
     */
    private static float lerpEased(float current, float target, float speed) {
        float diff = target - current;
        // 二次缓动：差距大时变化快，接近时变化慢
        return current + diff * speed;
    }

    // ========== 渲染查询 ==========

    /**
     * 渲染时调用，获取插值后的 alpha
     */
    public static float getAlpha(float partialTick) {
        return Mth.lerp(partialTick, prevAlpha, currentAlpha);
    }

    /**
     * 强制立即显示（外部触发）
     */
    public static void forceShow() {
        contentChangeTick = globalTick;
        currentAlpha = MAX_ALPHA.get().floatValue();
        prevAlpha = MAX_ALPHA.get().floatValue();
    }
}
