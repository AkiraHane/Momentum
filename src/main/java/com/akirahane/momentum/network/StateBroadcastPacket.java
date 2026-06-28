package com.akirahane.momentum.network;

import com.akirahane.momentum.core.state.StateType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

public record StateBroadcastPacket(int playerId, StateType stateType, int extraData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull StateBroadcastPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "broadcast_state"));

    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull StateBroadcastPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, StateBroadcastPacket::playerId,
                    ByteBufCodecs.VAR_INT, pkt -> pkt.stateType().getId(),
                    ByteBufCodecs.VAR_INT, StateBroadcastPacket::extraData,
                    (id, state, extra) -> new StateBroadcastPacket(id, StateType.fromId(state), extra)
            );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
