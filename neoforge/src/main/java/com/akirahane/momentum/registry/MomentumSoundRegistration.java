package com.akirahane.momentum.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Constants.MOD_ID;

public class MomentumSoundRegistration {
    private static final DeferredRegister<@NotNull SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(MomentumSounds.JET1_KEY.identifier().getPath(), SoundEvent::createVariableRangeEvent);
        SOUND_EVENTS.register(MomentumSounds.JET2_KEY.identifier().getPath(), SoundEvent::createVariableRangeEvent);
        SOUND_EVENTS.register(MomentumSounds.JET3_KEY.identifier().getPath(), SoundEvent::createVariableRangeEvent);
        SOUND_EVENTS.register(modEventBus);
    }
}
