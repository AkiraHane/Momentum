package com.akirahane.momentum.registry;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import static com.akirahane.momentum.Constants.MOD_ID;
import static com.akirahane.momentum.config.ServerConfig.DEFAULT_ENABLE_MANEUVER;

public class MomentumAttachments {

    public static final AttachmentType<Boolean> MOMENTUM_ENABLED = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "momentum_enabled"),
            builder -> builder.initializer(DEFAULT_ENABLE_MANEUVER)
                    .persistent(Codec.BOOL)
                    .copyOnDeath()
                    .syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
    );

    public static final AttachmentType<MomentumStateMachine> MOMENTUM_STATE = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "momentum_state")
    );

    public static void register() {
        // 入口调用此方法，触发静态字段中的附件注册。
    }
}
