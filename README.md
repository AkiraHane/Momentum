# Momentum

[简体中文](README_CN.md)

Momentum is a parkour movement mod for Minecraft. It adds momentum-preserving ground, air, wall, and water maneuvers, with matching first- and third-person animation effects.

The `1.3.0-beta` line introduces a shared multi-loader codebase for NeoForge and Fabric. It is intended for testing before the next stable release.

| | |
|---|---|
| **Mod ID** | `momentum` |
| **Current source version** | `1.3.0-beta` |
| **Minecraft** | `26.1.2` |
| **Loaders** | NeoForge `26.1.2.64-beta`; Fabric Loader `0.18.6` |
| **Java** | `25` |
| **Author** | AkiraHane |
| **License** | All Rights Reserved |

## Highlights in 1.3.0 beta

- Added a `common` module shared by the NeoForge and Fabric builds.
- Added a complete Fabric implementation, including Trinkets Updated support, configuration files, debug information, animations, camera effects, and multiplayer state synchronization.
- State changes are broadcast only to players tracking the entity, with an initial snapshot when tracking begins.
- Improved continuous wall-run → wall-kick → wall-run chaining. Wall kicks preserve existing momentum, use the intended input/view direction, and have a short re-entry grace period.
- The wall-kick acceleration cooldown now defaults to `10` ticks.
- Jet Booster wall running gradually damps both upward and downward vertical velocity toward zero. Normal wall running keeps its original gravity behavior; ledge locking remains a separate rule.
- Fixed ladder launch acceleration, dolphin-jump transitions near ladders, directional slope acceleration, slide retention, and Fabric camera-roll direction.

## Installation

Install the mod and its required dependencies on both the client and server for multiplayer.

1. Choose the Momentum JAR matching your loader: `momentum-neoforge-...jar` or `momentum-fabric-...jar`.
2. Install Player Animation Library for the same Minecraft version and loader.
3. Fabric users must also install Fabric API.
4. Curios on NeoForge and Trinkets Updated on Fabric are optional. They add a belt accessory slot for the Jet Booster; the vanilla legs slot works without them.

### Dependencies

| Dependency | Version | Requirement |
|---|---|---|
| Player Animation Library | `1.2.3+mc.26.1` | Required on both loaders |
| Fabric API | `0.145.4+26.1.2` | Required on Fabric |
| Curios | `15.0.0-beta.2+26.1.2` | Optional on NeoForge |
| Trinkets Updated | `4.0.0+26.1` | Optional on Fabric |

## Controls

All controls can be changed in Minecraft's key-binding menu.

| Default key | Action |
|---|---|
| `C` | Lower center: prone, slide, break-fall preparation, and vault into a low opening |
| `Shift + M` | Enable or disable Momentum movement for the local player |
| `Shift + N` | Show or hide contextual movement hints |
| Vanilla movement keys | Wall run, wall climb, wall kick, dodge, power jump, and swimming use the configured movement/jump/sprint/sneak keys |

## Movement

Momentum currently has 18 state-machine states: 17 movement states plus the vanilla fallback state. The highest-priority valid state is selected each client tick and synchronized through the server.

| Group | Actions |
|---|---|
| **Ground** | Walk, slide, prone, and power jump |
| **Air / landing** | Airborne movement, break-fall preparation, break fall, and dodge |
| **Wall** | Wall climb, wall slide, wall run, wall hang, wall kick, vault up, and vault into low openings |
| **Water** | Swimming and swim dash, including dolphin jumps out of the water |
| **Fallback** | Original state, used when Momentum is disabled or no maneuver matches |

### Physics and chaining

- Air drag, air steering, minimum action speeds, hunger consumption, and action cooldowns are configurable.
- Sliding reduces friction. Downhill movement accelerates gradually toward the detected slope direction and approaches a soft maximum speed; uphill movement loses speed.
- Sprinting on a ladder multiplies vertical movement, while positive climb speed is bounded to prevent repeated multiplication from launching the player upward.
- Wall-running preserves tangential speed and applies only a small wall-normal push. Wall kicks retain existing horizontal momentum instead of rewriting independent world X/Z components.
- Break-fall actions reduce fall damage and can chain into a slide when enough forward momentum remains.
- Dodge and swim dash share their configured recovery resource.

### Jet Booster

The Jet Booster can be equipped in the vanilla legs slot, a Curios belt slot, or a Trinkets Updated belt slot. When equipped it:

- increases movement speed, jump strength, and step height;
- enables mid-air dodge;
- reduces fall damage and effective fall distance;
- removes wall-run gravity away from ledges and smoothly damps vertical velocity toward zero;
- plays dedicated booster sounds during supported maneuvers.

The item can be enchanted and is crafted from the recipe bundled with the mod.

## Configuration

Most actions have both a server switch and a client switch. Both must be enabled for the action to activate.

- **NeoForge:** uses `ModConfigSpec`. Client settings are available from the loader's configuration screen, and server settings use the NeoForge server-config lifecycle.
- **Fabric:** writes validated `config/momentum-client.json` and `config/momentum-server.json`. The server configuration is synchronized to joining clients. Fabric does not yet provide an in-game configuration screen or live file reload; edit the JSON files and restart/rejoin.

Existing configuration files keep their saved values when code defaults change. For example, an existing `wallKickAccelerationCooldown: 20` must be changed manually to `10` if the new default is desired.

## Development

The project requires JDK 25. IntelliJ IDEA should delegate builds and tests to Gradle; IDEA's native builder cannot fully model the resource expansion used for loader metadata.

```bash
./gradlew build                       # Build common, Fabric, and NeoForge
./gradlew :fabric:runClient           # Fabric test client 1
./gradlew :fabric:runClient2          # Fabric test client 2
./gradlew :fabric:runServer           # Fabric test server
./gradlew :neoforge:runClient         # NeoForge test client 1
./gradlew :neoforge:runClient2        # NeoForge test client 2
./gradlew :neoforge:runServer         # NeoForge test server
./gradlew :neoforge:runData           # Generate NeoForge data/resources
```

On Windows, replace `./gradlew` with `gradlew.bat`. Loader JARs are produced in:

```text
fabric/build/libs/
neoforge/build/libs/
```

### Project layout

| Module | Responsibility |
|---|---|
| `common` | Movement state machine, physics/effects, shared packets, animations, assets, and loader-neutral Mixins for Minecraft 26.1.2 |
| `fabric` | Fabric entrypoints, lifecycle events, networking transport, attachments, JSON configuration, rendering hooks, and Trinkets integration |
| `neoforge` | NeoForge entrypoints/events, payload transport, attachments, `ModConfigSpec`, rendering events, and Curios integration |
| `build-logic` | Shared Gradle conventions and resource processing |

`common` is loader-neutral, not Minecraft-version-neutral. Minecraft API and Mixin changes are handled per Minecraft-version branch; logic with no Minecraft dependency can later move into a pure Java engine module. See [PORTING.md](PORTING.md) for the porting policy.

### Networking model

The local client evaluates movement transitions and sends compact state metadata to the server. The server applies the state and broadcasts it to tracking players plus self for replay compatibility. A state snapshot is sent when another player begins tracking the entity, so remote animations do not have to wait for the next transition.

## Automated publishing

Pushing a version tag runs the GitHub Actions workflow, builds both loader JARs, creates a GitHub Release, and publishes separate NeoForge and Fabric files to Modrinth and CurseForge.

The repository must define these Actions secrets:

- `MODRINTH_TOKEN`
- `CURSEFORGE_TOKEN`

The tag must exactly match the version assembled from `gradle.properties`:

```text
v<mod_version>.<mod_build>-<mod_prerelease>
```

For the current source version, the matching tag is `v1.3.0-beta`.

## Feedback

When reporting a problem, include the Momentum version, loader, Minecraft version, installed optional dependencies, and reproduction steps. Multiplayer synchronization reports should include whether the issue affects the local player, remote players, or both.

| Platform | Contact |
|---|---|
| QQ | `1796334524` |
| Bilibili | [@AkiraHane](https://space.bilibili.com/27666009) |

---

> This document was prepared with AI assistance and reviewed against the current source tree.
