package com.akirahane.momentum.client.hud;

import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HintManager {

    /** 按键连接方式 */
    public enum KeyJoin {
        AND("+"),
        OR("/");

        public final String separator;

        KeyJoin(String separator) {
            this.separator = separator;
        }
    }

    /** 单条按键提示，支持多个按键 */
    public record KeyHint(List<KeyMapping> keys, KeyJoin join, Component description) {

        // 单键
        public KeyHint(KeyMapping key, Component description) {
            this(List.of(key), KeyJoin.AND, description);
        }

        public KeyHint(KeyMapping key, String translationKey) {
            this(List.of(key), KeyJoin.AND, Component.translatable(translationKey));
        }

        // 多键 - 用 + 连接
        public static KeyHint and(Component description, KeyMapping... keys) {
            return new KeyHint(List.of(keys), KeyJoin.AND, description);
        }

        public static KeyHint and(String translationKey, KeyMapping... keys) {
            return new KeyHint(List.of(keys), KeyJoin.AND, Component.translatable(translationKey));
        }

        // 多键 - 用 / 连接
        public static KeyHint or(Component description, KeyMapping... keys) {
            return new KeyHint(List.of(keys), KeyJoin.OR, description);
        }

        public static KeyHint or(String translationKey, KeyMapping... keys) {
            return new KeyHint(List.of(keys), KeyJoin.OR, Component.translatable(translationKey));
        }
    }

    private static final Map<String, KeyHint> HINTS = new LinkedHashMap<>();

    // 全局开关
    @Getter
    private static boolean visible = true;

    public static void add(String id, KeyHint hint) {
        HINTS.put(id, hint);
    }

    public static void add(String id, KeyMapping key, String translationKey) {
        HINTS.put(id, new KeyHint(key, translationKey));
    }

    public static void add(String id, KeyMapping key, Component description) {
        HINTS.put(id, new KeyHint(key, description));
    }

    /**
     * 移除指定提示
     */
    public static void remove(String id) {
        HINTS.remove(id);
    }

    /**
     * 清空所有提示
     */
    public static void clear() {
        HINTS.clear();
    }

    /**
     * 检查是否包含指定提示
     */
    public static boolean contains(String id) {
        return HINTS.containsKey(id);
    }

    /**
     * 批量替换：清空后添加新的一批
     * 适合状态切换时一次性更新
     */
    public static void replaceAll(Map<String, KeyHint> newHints) {
        HINTS.clear();
        HINTS.putAll(newHints);
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
}
