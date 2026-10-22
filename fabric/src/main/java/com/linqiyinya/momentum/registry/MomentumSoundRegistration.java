package com.linqiyinya.momentum.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public class MomentumSoundRegistration {

    public static void register() {
        register(MomentumSounds.JET1_KEY);
        register(MomentumSounds.JET2_KEY);
        register(MomentumSounds.JET3_KEY);
    }

    private static void register(ResourceKey<SoundEvent> key) {
        Registry.register(BuiltInRegistries.SOUND_EVENT, key, SoundEvent.createVariableRangeEvent(key.identifier()));
    }
}
