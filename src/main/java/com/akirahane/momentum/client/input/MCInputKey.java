package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class MCInputKey {

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        MovementStateMachine machine = player.getData(InitAttachments.MOVEMENT_STATE);
        Map<String, boolean[]> map = machine.getContext().getInputBuffer();
        map.get("up")[machine.getContext().getInputBufferIndex()] = mc.options.keyUp.isDown();
        map.get("down")[machine.getContext().getInputBufferIndex()] = mc.options.keyDown.isDown();
        map.get("left")[machine.getContext().getInputBufferIndex()] = mc.options.keyLeft.isDown();
        map.get("right")[machine.getContext().getInputBufferIndex()] = mc.options.keyRight.isDown();
        map.get("jump")[machine.getContext().getInputBufferIndex()] = mc.options.keyJump.isDown();

    }
}
