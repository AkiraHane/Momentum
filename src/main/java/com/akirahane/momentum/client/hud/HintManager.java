package com.akirahane.momentum.client.hud;

import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HintManager {

    /**
     * 按键连接方式
     */
    public enum KeyJoin {
        AND("+"),
        OR("/");

        public final String separator;

        KeyJoin(String separator) {
            this.separator = separator;
        }
    }

    private static final Map<String, KeyHint> HINTS = new LinkedHashMap<>();

    // 全局开关
    @Getter
    private static boolean visible = true;

    public static void add(String id, KeyHint hint) {
        HINTS.put(id, hint);
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
