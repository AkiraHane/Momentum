# Momentum porting architecture

Momentum is being migrated incrementally so every step keeps the existing NeoForge build usable.

## Modules

- `common`: loader-independent code and resources for Minecraft 26.1.2. This module must not import Fabric or NeoForge APIs.
- `neoforge`: NeoForge entrypoints, events, configuration, networking registration, attachments, registries and Curios integration.
- `fabric`: Fabric entrypoints, lifecycle/network adapters, Fabric-only compatibility mixins and Trinkets Updated integration.

The dependency direction is always `fabric/neoforge -> common`. Common code must never reference a loader module.

## Migration order

1. Move trivially loader-independent code and resources to `common`.
2. Replace direct key binding access in movement states with an input snapshot.
3. Hide player movement-state storage behind a narrow platform interface.
4. Hide config, equipment, sound, animation and networking operations behind dedicated interfaces.
5. Move the state machine, movement context and loader-neutral mixins to `common`.
6. Add the Fabric module and implement its adapters.
7. Test client, dedicated server and publishing outputs for every loader.

## Current progress

- Complete: multi-module Gradle layout, shared assets/recipes, common constants and loader-specific artifact naming.
- Complete: loader-neutral configuration values with NeoForge load/reload synchronization.
- Complete: per-tick movement input snapshots; movement states no longer read NeoForge key mappings or `Minecraft.options` directly.
- Complete: platform hooks for loader-specific booster accessory detection, jump-power access, forced poses, contextual block properties, movement hints, custom sounds and local-player checks.
- Complete: attachment-backed state access is hidden behind the gameplay platform; the state machine, movement context, movement states, payload definitions and animation controller now live in `common`.
- Complete: loader-neutral entity/render mixins and their mixin configuration now live in `common`.
- Complete: shared payload codecs and loader-neutral packet handlers are paired with thin Fabric/NeoForge transport adapters; state changes are sent only to tracking players (plus self for replay recording), and a full state snapshot is sent when tracking begins.
- NeoForge-owned: attachment registration/storage, events, registries, config synchronization, Curios integration and NeoForge PAL bootstrap.
- Complete: Fabric stores movement state on the player lifecycle and persists, copies-on-death and synchronizes the per-player enabled flag through Fabric attachments.
- Complete: fall-damage adjustment and dodge immunity rules are shared in `common`, with loader-specific NeoForge events and Fabric hooks.
- Complete: the jet-booster item properties are shared, while both loaders register the item themselves; NeoForge supports Curios and Fabric supports Trinkets Updated in addition to the vanilla legs slot.
- Complete: Fabric registers and resolves all three custom jet sound events through the shared logical sound abstraction.
- Complete: Fabric applies the shared camera roll, momentum roll, FOV bonus and first-person hand transforms through client-only rendering mixins.
- Complete: Fabric loads validated client/server JSON configs, synchronizes authoritative server movement settings to joining clients and exposes a broadcast path for future live config reloads.
- Complete: Fabric renders the logical movement hints with the same key sprites/fade parameters and persists the Shift+N visibility toggle.
- Complete: both loaders register the shared movement diagnostics in Minecraft's configurable debug screen; entries remain disabled by default.
- Complete: tagged GitHub builds attach both loader jars and publish separate NeoForge/Fabric versions to Modrinth and CurseForge with loader-specific dependencies.
- Fabric pending before release: an in-game config screen.

## Version policy

Each major Minecraft target lives on its own branch. Within a branch, loader modules share one Minecraft-facing common module. Logic that becomes completely independent of `net.minecraft` can later move into a Java-only `engine` module targeting the lowest supported Java version.
