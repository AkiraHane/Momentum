package com.linqiyinya.momentum.client.tick;

import com.linqiyinya.momentum.Constants;
import com.linqiyinya.momentum.core.state.BaseState;
import com.linqiyinya.momentum.core.state.MomentumStateMachine;
import com.linqiyinya.momentum.network.StateTransitionPacket;
import com.linqiyinya.momentum.platform.Services;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class MomentumClientTickHelper {

    /** 原版玩家 tick 前，本地玩家计算状态转换，远程玩家更新视觉数据。 */
    public static void onPlayerTickPre(AbstractClientPlayer player) {
        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        if (player instanceof LocalPlayer) {
            BaseState state = stateMachine.clientTick(player);
            if (state != null) {
                var context = stateMachine.getContext();
                StateTransitionPacket packet = new StateTransitionPacket(
                        state.getStateType(), context.getTransitionExtraData(), context.getTransitionWallData()
                );
                Services.PLATFORM.sendToServer(packet);
                // 状态转换或互动墙面变化时发送一次。
                context.setTransitionExtraData(-1);
                context.setTransitionWallData((byte) -1);
                Constants.LOG.debug("[NET] C2S_SEND side=client player={} dimension={} state={} extra={} wall={} resetExtra={} resetWall={}",
                        player.getId(), player.level().dimension().identifier(), packet.stateType(), packet.extraData(), packet.wallData(),
                        context.getTransitionExtraData(), context.getTransitionWallData());
            }
        } else {
            stateMachine.clientTickRemote(player);
        }
    }

    /** 原版玩家 tick 后记录实际位移，供下一 tick 计算和动画插值使用。 */
    public static void onPlayerTickPost(AbstractClientPlayer player) {
        var context = Services.PLATFORM.getOrCreateStateMachine(player).getContext();
        context.setOldSpeed(context.getSpeed());
        context.setSpeed(new Vec3(
                player.getX() - player.xOld,
                player.getY() - player.yOld,
                player.getZ() - player.zOld
        ));
        context.setOldDeltaMovement(player.getDeltaMovement());
    }
}
