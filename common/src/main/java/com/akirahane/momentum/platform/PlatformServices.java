package com.akirahane.momentum.platform;

import com.akirahane.momentum.platform.client.ClientPlatform;

import java.util.Objects;

/** Explicit loader bootstrap point for narrow platform adapters. */
public final class PlatformServices {
    private static volatile ClientPlatform client = ClientPlatform.NOOP;
    private static volatile GameplayPlatform gameplay = GameplayPlatform.NOOP;

    private PlatformServices() {
    }

    public static ClientPlatform client() {
        return client;
    }

    public static GameplayPlatform gameplay() {
        return gameplay;
    }

    public static void installClient(ClientPlatform implementation) {
        client = Objects.requireNonNull(implementation);
    }

    public static void installGameplay(GameplayPlatform implementation) {
        gameplay = Objects.requireNonNull(implementation);
    }
}
