package com.akirahane.momentum.fabric.client.platform;

import com.akirahane.momentum.fabric.init.FabricSounds;
import com.akirahane.momentum.fabric.client.hud.FabricHintManager;
import com.akirahane.momentum.platform.client.ClientPlatform;
import com.akirahane.momentum.platform.client.MovementInput;
import com.akirahane.momentum.platform.client.MomentumSound;
import com.akirahane.momentum.platform.client.MovementHint;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class FabricClientPlatform implements ClientPlatform {
    private final KeyMapping lowerCenter;

    public FabricClientPlatform(KeyMapping lowerCenter) {
        this.lowerCenter = lowerCenter;
    }

    @Override
    public MovementInput captureMovementInput(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        float strafe = 0.0F;
        float forward = 0.0F;
        if (player instanceof LocalPlayer localPlayer) {
            var move = localPlayer.input.getMoveVector();
            strafe = move.x;
            forward = move.y;
        }
        return new MovementInput(
                minecraft.options.keyUp.isDown(),
                minecraft.options.keyDown.isDown(),
                minecraft.options.keyLeft.isDown(),
                minecraft.options.keyRight.isDown(),
                minecraft.options.keyJump.isDown(),
                minecraft.options.keySprint.isDown(),
                minecraft.options.keyShift.isDown(),
                lowerCenter.isDown(),
                strafe,
                forward
        );
    }

    @Override
    public float fovEffectScale() {
        return Minecraft.getInstance().options.fovEffectScale().get().floatValue();
    }

    @Override
    public boolean isLocalPlayer(Player player) {
        return Minecraft.getInstance().player == player;
    }

    @Override
    public void clearHints() {
        FabricHintManager.clear();
    }

    @Override
    public void addHint(MovementHint hint) {
        FabricHintManager.add(hint);
    }

    @Override
    public boolean containsHint(MovementHint hint) {
        return FabricHintManager.contains(hint);
    }

    @Override
    public void playSound(Player player, MomentumSound sound, float volume, float pitch) {
        var event = switch (sound) {
            case JET1 -> FabricSounds.JET1;
            case JET2 -> FabricSounds.JET2;
            case JET3 -> FabricSounds.JET3;
        };
        player.playSound(event, volume, pitch);
    }
}
