package com.akirahane.momentum.client.network;

import com.akirahane.momentum.Constants;
import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.network.StateBroadcastPacket;
import com.akirahane.momentum.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MomentumClientNetworkHelper {

    public static void onStateBroadcast(StateBroadcastPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        int receiver = mc.player == null ? -1 : mc.player.getId();
        Constants.LOG.debug("[NET] S2C_RECEIVE side=client receiver={} player={} dimension={} state={} extra={} wall={}",
                receiver, packet.playerId(), mc.level == null ? null : mc.level.dimension().identifier(),
                packet.stateType(), packet.extraData(), packet.wallData());
        if (mc.level == null) {
            Constants.LOG.debug("[NET] S2C_IGNORE receiver={} player={} reason=no_level", receiver, packet.playerId());
            return;
        }

        var entity = mc.level.getEntity(packet.playerId());
        if (!(entity instanceof Player player)) {
            Constants.LOG.debug("[NET] S2C_IGNORE receiver={} player={} reason=entity_missing", receiver, packet.playerId());
            return;
        }
        if (player == mc.player) {
            Constants.LOG.debug("[NET] S2C_IGNORE receiver={} player={} reason=local_player", receiver, packet.playerId());
            return;
        }

        MomentumStateMachine stateMachine = Services.PLATFORM.getOrCreateStateMachine(player);
        stateMachine.getContext().setTransitionExtraData(packet.extraData());
        stateMachine.getContext().setTransitionWallData(packet.wallData());
        stateMachine.setStateFromClient(packet.stateType(), player);
        Constants.LOG.debug("[NET] S2C_APPLY receiver={} player={} state={} extra={} wall={}",
                receiver, player.getId(), stateMachine.getCurrentState().getStateType(),
                stateMachine.getContext().getTransitionExtraData(), stateMachine.getContext().getTransitionWallData());
    }
}
