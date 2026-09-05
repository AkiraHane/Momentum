package com.akirahane.momentum.fabric.init;

import com.akirahane.momentum.MomentumConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class FabricSounds {
    public static final SoundEvent JET1 = register("jet1");
    public static final SoundEvent JET2 = register("jet2");
    public static final SoundEvent JET3 = register("jet3");

    private FabricSounds() {
    }

    public static void register() {
        // Loading this class registers all sound events.
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MomentumConstants.MOD_ID, path);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
