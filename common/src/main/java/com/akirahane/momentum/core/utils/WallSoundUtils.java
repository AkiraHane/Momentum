package com.akirahane.momentum.core.utils;

import com.akirahane.momentum.core.context.PlayerMomentumContext;
import com.akirahane.momentum.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class WallSoundUtils {
    public static final Function<SoundType, SoundEvent> STEP = SoundType::getStepSound,
            HIT = SoundType::getHitSound,
            BREAK = SoundType::getBreakSound,
            PLACE = SoundType::getPlaceSound,
            FALL = SoundType::getFallSound;

    public static void playWallSound(Player player, PlayerMomentumContext context, Function<SoundType, SoundEvent> kind, float volumeMultiplier, float pitchMultiplier) {
        if (context.getWallBlocks().isEmpty()) return;

        Level level = player.level();
        BlockPos pos = context.getWallBlocks().get(level.getRandom().nextInt(context.getWallBlocks().size()));
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        SoundType soundType = Services.PLATFORM.getBlockSoundType(state, player, pos);
        player.playSound(
                kind.apply(soundType),
                soundType.getVolume() * volumeMultiplier,
                soundType.getPitch() * pitchMultiplier
        );
    }
}
