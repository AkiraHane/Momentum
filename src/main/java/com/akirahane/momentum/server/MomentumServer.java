package com.akirahane.momentum.server;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.common.state.MovementStateMachine;
import com.akirahane.momentum.core.init.InitAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// 此处的值应与 META-INF/neoforge.mods.toml 文件中的条目匹配
@Mod(value = Momentum.MODID, dist = Dist.DEDICATED_SERVER)
@EventBusSubscriber(modid = Momentum.MODID, value = Dist.DEDICATED_SERVER)
public class MomentumServer {

    // 服务端驱动 状态机处理放在原版逻辑之前, 但是要晚于玩家输入处理
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MovementStateMachine sm = player.getData(InitAttachments.MOVEMENT_STATE);
            sm.serverTick(player);
        }
    }
}
