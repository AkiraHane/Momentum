package com.akirahane.momentum.init;

import com.akirahane.momentum.core.state.MovementStateMachine;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.akirahane.momentum.Momentum.MODID;
import static com.akirahane.momentum.config.ServerConfig.DEFAULT_ENABLE_MANEUVER;

public class InitAttachments {
    // 注册持久化
    private static final DeferredRegister<@NotNull AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    public static final Supplier<AttachmentType<@NotNull Boolean>> MOMENTUM_ENABLED =
            ATTACHMENT_TYPES.register("momentum_enabled",
                    () -> AttachmentType.builder(DEFAULT_ENABLE_MANEUVER)
                            .serialize(Codec.BOOL.fieldOf("momentum_enabled"))
                            .copyOnDeath()
                            .sync(ByteBufCodecs.BOOL.cast())
                            .build()
            );
    public static final Supplier<AttachmentType<@NotNull MovementStateMachine>> MOVEMENT_STATE =
            ATTACHMENT_TYPES.register("movement_state",
                    () -> AttachmentType.builder(
                                    holder -> new MovementStateMachine((Player) holder)
                            ).build()
            );

    public static void register(IEventBus modEventBus) {
        // 注册持久化数据
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
