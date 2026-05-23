package com.akirahane.momentum.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

public record SyncMomentumEnabledPacket(boolean enabled) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull SyncMomentumEnabledPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_momentum_enabled"));

    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull SyncMomentumEnabledPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SyncMomentumEnabledPacket::enabled,
                    SyncMomentumEnabledPacket::new
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
