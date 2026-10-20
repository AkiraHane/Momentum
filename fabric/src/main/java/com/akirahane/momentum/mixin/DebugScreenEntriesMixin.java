package com.akirahane.momentum.mixin;

import com.akirahane.momentum.client.debug.MomentumDebugEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenEntries.class)
public abstract class DebugScreenEntriesMixin {

    @Shadow
    private static Identifier register(Identifier identifier, DebugScreenEntry entry) {
        throw new AssertionError();
    }

    // 在原版调试项初始化后注册，使已保存的显示设置加载时能找到此项。
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void momentum$registerDebugEntries(CallbackInfo ci) {
        // 原版将预设中未指定的条目视为 NEVER，保持默认关闭。
        register(MomentumDebugEntry.ENTRY_ID, new MomentumDebugEntry());
    }
}
