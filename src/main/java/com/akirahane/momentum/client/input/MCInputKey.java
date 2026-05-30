package com.akirahane.momentum.client.input;

import com.akirahane.momentum.Momentum;
import com.akirahane.momentum.core.state.MovementStateMachine;
import com.akirahane.momentum.init.InitAttachments;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

import java.util.HashSet;

import static com.akirahane.momentum.core.context.PlayerMovementContext.*;

@EventBusSubscriber(modid = Momentum.MODID, value = Dist.CLIENT)
public class MCInputKey {

    protected static final Logger LOGGER = LogUtils.getLogger();

    private static boolean wasUp = false;
    private static boolean wasDown = false;
    private static boolean wasLeft = false;
    private static boolean wasRight = false;
    private static boolean wasJump = false;

    // 每tick检测
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        MovementStateMachine machine = player.getData(InitAttachments.MOVEMENT_STATE);
        HashSet<String>[] list = machine.getContext().getInputBuffer();
        machine.getContext().setInputBufferIndex(
                (machine.getContext().getInputBufferIndex() + 1) % KEYS.length
        );
        list[machine.getContext().getInputBufferIndex()].clear();
        if (mc.options.keyUp.isDown() && !wasUp) {
            list[machine.getContext().getInputBufferIndex()].add(KEYS[UP]);
        }
        wasUp = mc.options.keyUp.isDown();
        if (mc.options.keyDown.isDown() && !wasDown) {
            list[machine.getContext().getInputBufferIndex()].add(KEYS[DOWN]);
        }
        wasDown = mc.options.keyDown.isDown();
        if (mc.options.keyLeft.isDown() && !wasLeft) {
            list[machine.getContext().getInputBufferIndex()].add(KEYS[LEFT]);
        }
        wasLeft = mc.options.keyLeft.isDown();
        if (mc.options.keyRight.isDown() && !wasRight) {
            list[machine.getContext().getInputBufferIndex()].add(KEYS[RIGHT]);
        }
        wasRight = mc.options.keyRight.isDown();
        if (mc.options.keyJump.isDown() && !wasJump) {
            list[machine.getContext().getInputBufferIndex()].add(KEYS[JUMP]);
        }
        wasJump = mc.options.keyJump.isDown();
    }
}
