package com.akirahane.momentum.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

import static com.akirahane.momentum.Constants.MOD_ID;

public class MomentumSounds {

    public static final ResourceKey<SoundEvent> JET1_KEY = ResourceKey.create(
            Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "jet1")
    );
    public static final ResourceKey<SoundEvent> JET2_KEY = ResourceKey.create(
            Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "jet2")
    );
    public static final ResourceKey<SoundEvent> JET3_KEY = ResourceKey.create(
            Registries.SOUND_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "jet3")
    );

    public static final Supplier<SoundEvent> JET1 = () -> BuiltInRegistries.SOUND_EVENT.getValueOrThrow(JET1_KEY);
    public static final Supplier<SoundEvent> JET2 = () -> BuiltInRegistries.SOUND_EVENT.getValueOrThrow(JET2_KEY);
    public static final Supplier<SoundEvent> JET3 = () -> BuiltInRegistries.SOUND_EVENT.getValueOrThrow(JET3_KEY);
}
