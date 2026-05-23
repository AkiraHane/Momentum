package com.akirahane.momentum.core.compat.curios;

import net.neoforged.fml.ModList;

/**
 * Curios API 兼容层入口
 * <p>
 * 仅在 Curios 模组存在时初始化，提供腰饰槽位支持。
 * 通过 ModList 检测避免硬依赖。
 */
public final class CuriosCompat {

    private static final String CURIOS_MODID = "curios";

    private CuriosCompat() {
    }

    /**
     * 检查 Curios 是否已加载
     */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID);
    }
}
