package com.akirahane.momentum.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

public record ToggleMomentumPacket() implements CustomPacketPayload {

    public static final ToggleMomentumPacket INSTANCE = new ToggleMomentumPacket();

    public static final CustomPacketPayload.Type<@NotNull ToggleMomentumPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "toggle_momentum"));

    // 无数据的包用unit FriendlyByteBuf 是MC包装的 更友好
    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull ToggleMomentumPacket> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
