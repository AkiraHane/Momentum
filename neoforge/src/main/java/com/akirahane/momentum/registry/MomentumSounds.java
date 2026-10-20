package com.akirahane.momentum.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static com.akirahane.momentum.Constants.MOD_ID;

public class MomentumSounds {
    public static final DeferredRegister<@NotNull SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);
    // 独立的声音事件
    public static final Holder<@NotNull SoundEvent> JET1 = SOUND_EVENTS.register(
            "jet1",
            SoundEvent::createVariableRangeEvent
    );

    public static final Holder<@NotNull SoundEvent> JET2 = SOUND_EVENTS.register(
            "jet2",
            SoundEvent::createVariableRangeEvent
    );

    public static final Holder<@NotNull SoundEvent> JET3 = SOUND_EVENTS.register(
            "jet3",
            SoundEvent::createVariableRangeEvent
    );
}
