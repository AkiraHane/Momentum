package com.akirahane.momentum.client.hud;

import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HintManager {

    // ========== 数据结构 ==========

    public sealed interface Element {
    }

    public record KeyElement(KeyMapping key) implements Element {
    }

    public record TextElement(String text) implements Element {
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
            for (int i = 0; i < keys.length; i++) {
                if (i > 0) list.add(new TextElement(sep));
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
                elements.add(new TextElement("+"));
                return this;
            }

            public Builder slash() {
                elements.add(new TextElement("/"));
                return this;
            }

            public Builder text(String text) {
                elements.add(new TextElement(text));
                return this;
            }

            public KeyHint build() {
                return new KeyHint(List.copyOf(elements), description);
            }
        }
    }

    // ========== 状态管理 ==========

    private static final Map<String, KeyHint> HINTS = new LinkedHashMap<>();
    @Getter
    private static boolean visible = true;

    // 内容签名，用于检测提示集变化
    private static int lastHintsHash = 0;

    // 各种 tick 计数
    private static int contentChangeTick = 0;    // 提示内容上次变化时间
    private static int stillTick = 0;            // 玩家停止移动持续时间
    private static int movingTick = 0;           // 玩家移动持续时间

    // alpha 状态
    private static float currentAlpha = 0f;
    private static float prevAlpha = 0f;
    private static int globalTick = 0;

    // 配置参数
    private static final float MIN_ALPHA_WHEN_MOVING = 0.20f;  // 移动时最低透明度
    private static final float MAX_ALPHA = 1.0f;
    private static final int FRESH_DURATION = 80;              // 内容变化后高亮持续时间（4秒）
    private static final int IDLE_DELAY = 30;                  // 静止多久后淡入（1.5秒）
    private static final double MOVE_THRESHOLD_SQR = 0.005;    // 视为移动的速度阈值

    // 缓动速度（值越小越慢）
    private static final float FADE_IN_SPEED = 0.08f;
    private static final float FADE_OUT_SPEED = 0.18f;

    // ========== 公开 API ==========

    public static void add(String id, KeyHint hint) {
        KeyHint existing = HINTS.get(id);
        if (existing != hint) {
            HINTS.put(id, hint);
        }
    }

    public static void remove(String id) {
        HINTS.remove(id);
    }

    public static void removeByPrefix(String prefix) {
        HINTS.keySet().removeIf(id -> id.startsWith(prefix));
    }

    public static void clear() {
        HINTS.clear();
    }

    public static Collection<KeyHint> getAll() {
        return HINTS.values();
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

        // 1. 检测提示内容变化
        int currentHash = HINTS.keySet().hashCode();
        if (currentHash != lastHintsHash) {
            lastHintsHash = currentHash;
            contentChangeTick = globalTick;
        }

        // 2. 检测玩家移动状态
        Vec3 movement = player.getDeltaMovement();
        boolean moving = movement.horizontalDistanceSqr() > MOVE_THRESHOLD_SQR
                || Math.abs(movement.y) > 0.1;

        if (moving) {
            stillTick = 0;
            movingTick++;
        } else {
            stillTick++;
            movingTick = 0;
        }

        // 3. 计算目标 alpha
        float targetAlpha = computeTargetAlpha(moving);

        // 4. 平滑过渡（不对称速度）
        float speed = (currentAlpha < targetAlpha) ? FADE_IN_SPEED : FADE_OUT_SPEED;
        currentAlpha = lerpEased(currentAlpha, targetAlpha, speed);

        // 防止浮点抖动
        if (Math.abs(currentAlpha - targetAlpha) < 0.001f) {
            currentAlpha = targetAlpha;
        }
    }

    private static float computeTargetAlpha(boolean moving) {
        if (HINTS.isEmpty() || !visible) return 0f;

        int sinceChange = globalTick - contentChangeTick;

        // 优先级 1：内容刚变化（教学时机），强制显示
        if (sinceChange < FRESH_DURATION) {
            return MAX_ALPHA;
        }

        // 优先级 2：玩家静止超过阈值，显示提示
        if (stillTick >= IDLE_DELAY) {
            return MAX_ALPHA;
        }

        // 优先级 3：玩家在移动，降到最低可见度（不消失，做视觉锚点）
        if (moving) {
            return MIN_ALPHA_WHEN_MOVING;
        }

        // 默认：中等透明度
        return MIN_ALPHA_WHEN_MOVING;
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
     * 渲染时调用，判断是否需要绘制
     */
    public static boolean shouldRender(float partialTick) {
        return getAlpha(partialTick) > 0.01f;
    }

    /**
     * 强制立即显示（外部触发）
     */
    public static void forceShow() {
        contentChangeTick = globalTick;
        currentAlpha = MAX_ALPHA;
        prevAlpha = MAX_ALPHA;
    }

    /**
     * 通知"玩家执行了动作"，让提示稍微淡化（强化反馈）
     */
    public static void notifyActionPerformed() {
        // 让内容变化时间提前，更快进入低透明度状态
        contentChangeTick = globalTick - FRESH_DURATION + 10;
    }
}
