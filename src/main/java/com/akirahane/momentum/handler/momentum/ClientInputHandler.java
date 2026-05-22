package com.akirahane.momentum.handler.momentum;

import com.akirahane.momentum.init.ModKeyMappings;
import com.akirahane.momentum.network.ToggleMomentumPacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import static com.akirahane.momentum.Momentum.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class ClientInputHandler {

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // 动量模式切换 - 单次触发
        if (ModKeyMappings.CHANGE_MOMENTUM_KEY_MAPPING.get().consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleMomentumPacket());
        }
    }

//    // 每tick检测
//    @SubscribeEvent
//    public static void onKeyInput(InputEvent.Key event) {
//        // C键 - 需要喂给预输入缓冲
//        if (ModKeyMappings.LOWER_CENTER_KEY_MAPPING.isDown()) {
//            InputBuffer.buffer(InputAction.LOWER_CENTER);
//        }
//    }
}
