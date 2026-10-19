package com.akirahane.momentum.network;

import com.akirahane.momentum.core.state.StateType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Momentum.MODID;

// 客户端状态机切换状态后, 给服务器发送 (服务器只考虑属性状态 不参与计算状态转换)
// extraData: 附加数据, 用于传递客户端才有的信息 (如 Dodge 方向: 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT)
// wallData: 墙面同步数据 (bit 0-2: wallNormal索引, bit 3: inputWallAngle左右标志; -1=无墙面数据)
public record StateTransitionPacket(StateType stateType, int extraData, byte wallData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<@NotNull StateTransitionPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(MODID, "sync_momentum_state"));

    public static final StreamCodec<@NotNull FriendlyByteBuf, @NotNull StateTransitionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, pkt -> pkt.stateType.getId(),
                    ByteBufCodecs.VAR_INT, StateTransitionPacket::extraData,
                    ByteBufCodecs.BYTE, StateTransitionPacket::wallData,
                    (state, extra, wall) -> new StateTransitionPacket(
                            StateType.fromId(state), extra, wall
                    )
            );


    @Override
    public CustomPacketPayload.@NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
