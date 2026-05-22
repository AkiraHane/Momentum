package com.akirahane.momentum.init;

import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.akirahane.momentum.Momentum.MODID;

public class ModAttachments {
    // 注册持久化
    private static final DeferredRegister<@NotNull AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    public static final Supplier<AttachmentType<@NotNull Boolean>> MOMENTUM_ENABLED =
            ATTACHMENT_TYPES.register("momentum_enabled",
                    () -> AttachmentType.builder(() -> false)
                            .serialize(Codec.BOOL.fieldOf("momentum_enabled"))
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus modEventBus) {
        // 注册持久化数据
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
