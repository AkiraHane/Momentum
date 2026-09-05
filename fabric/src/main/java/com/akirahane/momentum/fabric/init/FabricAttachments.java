package com.akirahane.momentum.fabric.init;

import com.akirahane.momentum.MomentumConstants;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import static com.akirahane.momentum.platform.config.MomentumServerConfig.DEFAULT_ENABLE_MANEUVER;

public final class FabricAttachments {
    public static final AttachmentType<Boolean> MOMENTUM_ENABLED = AttachmentRegistry.create(
            id("momentum_enabled"),
            builder -> builder
                    .initializer(DEFAULT_ENABLE_MANEUVER::getAsBoolean)
                    .persistent(Codec.BOOL)
                    .copyOnDeath()
                    .syncWith(ByteBufCodecs.BOOL.cast(), AttachmentSyncPredicate.targetOnly()));

    public static final AttachmentType<MovementStateMachine> MOVEMENT_STATE =
            AttachmentRegistry.create(id("movement_state"));

    private FabricAttachments() {
    }

    public static void register() {
        // Loading this class registers both attachment types.
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, path);
    }
}
