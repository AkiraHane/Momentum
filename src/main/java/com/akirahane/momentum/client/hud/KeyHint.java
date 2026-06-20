package com.akirahane.momentum.client.hud;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public record KeyHint(List<Element> elements, Component description) {

    /** 提示中的元素（按键 或 分隔符文本） */
    public sealed interface Element {}
    public record KeyElement(KeyMapping key) implements Element {}
    public record TextElement(String text) implements Element {}

    // === 简单工厂（向后兼容）===

    public static KeyHint single(KeyMapping key, String translationKey) {
        return new KeyHint(
                List.of(new KeyElement(key)),
                Component.translatable(translationKey));
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

    // === Builder（混合连接用这个）===

    public static Builder builder(String translationKey) {
        return new Builder(Component.translatable(translationKey));
    }

    public static Builder builder(Component description) {
        return new Builder(description);
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
