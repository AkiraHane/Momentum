package com.akirahane.momentum.core.network;

import com.akirahane.momentum.core.common.state.MovementStateType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

public record StateTransitionPacket(MovementStateType stateType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull StateTransitionPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_momentum_state"));

    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull StateTransitionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, pkt -> pkt.stateType.getId(),
                    (state) -> new StateTransitionPacket(
                            MovementStateType.fromId(state)
                    )
            );


    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
