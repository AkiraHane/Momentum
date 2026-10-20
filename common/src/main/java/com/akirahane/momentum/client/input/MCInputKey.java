package com.akirahane.momentum.client.input;

import com.akirahane.momentum.core.state.MomentumStateMachine;
import com.akirahane.momentum.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.HashSet;

import static com.akirahane.momentum.core.context.PlayerMomentumContext.*;

public class MCInputKey {

    private static boolean wasUp = false;
    private static boolean wasDown = false;
    private static boolean wasLeft = false;
    private static boolean wasRight = false;
    private static boolean wasJump = false;
    private static boolean wasSprint = false;
    private static boolean wasShift = false;

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        MomentumStateMachine machine = Services.PLATFORM.getOrCreateStateMachine(player);
        HashSet<Integer>[] list = machine.getContext().getInputBuffer();
        machine.getContext().setInputBufferIndex(
                (machine.getContext().getInputBufferIndex() + 1) % machine.getContext().getInputBufferSize()
        );
        list[machine.getContext().getInputBufferIndex()].clear();
        if (mc.options.keyUp.isDown() && !wasUp) {
            list[machine.getContext().getInputBufferIndex()].add(UP);
        }
        wasUp = mc.options.keyUp.isDown();
        if (mc.options.keyDown.isDown() && !wasDown) {
            list[machine.getContext().getInputBufferIndex()].add(DOWN);
        }
        wasDown = mc.options.keyDown.isDown();
        if (mc.options.keyLeft.isDown() && !wasLeft) {
            list[machine.getContext().getInputBufferIndex()].add(LEFT);
        }
        wasLeft = mc.options.keyLeft.isDown();
        if (mc.options.keyRight.isDown() && !wasRight) {
            list[machine.getContext().getInputBufferIndex()].add(RIGHT);
        }
        wasRight = mc.options.keyRight.isDown();
        if (mc.options.keyJump.isDown() && !wasJump) {
            list[machine.getContext().getInputBufferIndex()].add(JUMP);
        }
        wasJump = mc.options.keyJump.isDown();
        if (mc.options.keySprint.isDown() && !wasSprint) {
            list[machine.getContext().getInputBufferIndex()].add(SPRINT);
        }
        wasSprint = mc.options.keySprint.isDown();
        if (mc.options.keyShift.isDown() && !wasShift) {
            list[machine.getContext().getInputBufferIndex()].add(SHIFT);
        }
        wasShift = mc.options.keyShift.isDown();

        if (mc.options.keyShift.isDown()){
            list[machine.getContext().getInputBufferIndex()].add(SHIFT_HOLD);
        }
    }
}
