package com.akirahane.momentum.fabric.network;

import com.akirahane.momentum.MomentumConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public record FabricServerConfigPacket(String json) implements CustomPacketPayload {
    public static final Type<@NotNull FabricServerConfigPacket> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, "server_config"));
    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull FabricServerConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,
                    FabricServerConfigPacket::json,
                    FabricServerConfigPacket::new);

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
