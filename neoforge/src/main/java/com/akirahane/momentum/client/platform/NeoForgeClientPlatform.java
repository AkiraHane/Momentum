package com.akirahane.momentum.client.platform;

import com.akirahane.momentum.client.input.LowerCenterKey;
import com.akirahane.momentum.client.hud.HintManager;
import com.akirahane.momentum.client.hud.WallHangHints;
import com.akirahane.momentum.client.init.InitSounds;
import com.akirahane.momentum.platform.client.ClientPlatform;
import com.akirahane.momentum.platform.client.MovementHint;
import com.akirahane.momentum.platform.client.MovementInput;
import com.akirahane.momentum.platform.client.MomentumSound;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public final class NeoForgeClientPlatform implements ClientPlatform {
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
                LowerCenterKey.LOWER_CENTER.get().isDown(),
                strafe,
                forward
        );
    }

    @Override
    public float fovEffectScale() {
        return Minecraft.getInstance().options.fovEffectScale().get().floatValue();
    }

    @Override
    public void clearHints() {
        HintManager.clear();
    }

    @Override
    public void addHint(MovementHint hint) {
        HintManager.add(resolveHint(hint));
    }

    @Override
    public boolean containsHint(MovementHint hint) {
        return HintManager.contains(resolveHint(hint));
    }

    @Override
    public boolean isLocalPlayer(Player player) {
        return Minecraft.getInstance().player == player;
    }

    @Override
    public void playSound(Player player, MomentumSound sound, float volume, float pitch) {
        var event = switch (sound) {
            case JET1 -> InitSounds.JET1.value();
            case JET2 -> InitSounds.JET2.value();
            case JET3 -> InitSounds.JET3.value();
        };
        player.playSound(event, volume, pitch);
    }

    private static HintManager.KeyHint resolveHint(MovementHint hint) {
        return switch (hint) {
            case BREAK_FALL_READY -> WallHangHints.BREAK_FALL_READY;
            case BREAK_FALL_READY_EGG -> WallHangHints.BREAK_FALL_READY_EGG;
            case PRONE -> WallHangHints.PRONE;
            case SLIDE -> WallHangHints.SLIDE;
            case DODGE_DIR_DOUBLE -> WallHangHints.DODGE_DIR_DOUBLE;
            case DODGE_SPRINT_CLICK -> WallHangHints.DODGE_SPRINT_CLICK;
            case DODGE_SPRINT_DOUBLE -> WallHangHints.DODGE_SPRINT_DOUBLE;
            case VAULT_IN -> WallHangHints.VAULT_IN;
            case VAULT_IN_STAND -> WallHangHints.VAULT_IN_STAND;
            case VAULT_UP -> WallHangHints.VAULT_UP;
            case WALL_CLIMB -> WallHangHints.WALL_CLIMB;
            case WALL_SLIDE -> WallHangHints.WALL_SLIDE;
            case WALL_HANG -> WallHangHints.WALL_HANG;
            case WALL_KICK -> WallHangHints.WALL_KICK;
            case POWER_JUMP_READY -> WallHangHints.POWER_JUMP_READY;
            case POWER_JUMP -> WallHangHints.POWER_JUMP;
            case WALL_RUN -> WallHangHints.WALL_RUN;
            case WALL_RUN_HOLD -> WallHangHints.WALL_RUN_HOLD;
            case TOGGLE_HINT -> WallHangHints.TOGGLE_HINT;
            case ORIGINAL_STATE -> WallHangHints.ORIGINAL_STATE;
            case CLIMB_ACCELERATION -> WallHangHints.CLIMB_ACCELERATION;
            case SWIM -> WallHangHints.SWIM;
            case SWIM_HOLD -> WallHangHints.SWIM_HOLD;
            case SWIM_ACTIVE -> WallHangHints.SWIM_ACTIVE;
            case PUSH -> WallHangHints.PUSH;
            case PUSH_UP -> WallHangHints.PUSH_UP;
            case SLOW_FALL -> WallHangHints.SLOW_FALL;
            case AIR_JUMP -> WallHangHints.AIR_JUMP;
            case WATER_RUN -> WallHangHints.WATER_RUN;
        };
    }
}
