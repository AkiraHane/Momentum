package com.akirahane.momentum.core.network;

import com.akirahane.momentum.core.common.state.StateType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

// 客户端状态机切换状态后, 给服务器发送 (服务器只考虑属性状态 不参与计算状态转换)
public record StateTransitionPacket(StateType stateType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull StateTransitionPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_momentum_state"));

    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull StateTransitionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, pkt -> pkt.stateType.getId(),
                    (state) -> new StateTransitionPacket(
                            StateType.fromId(state)
                    )
            );


    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
