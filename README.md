# Momentum

[中文版本](README_CN.md)

A Minecraft NeoForge mod that adds parkour movement and combat maneuver actions.

| | |
|---|---|
| **Mod ID** | `momentum` |
| **Version** | `1.1.2-beta` |
| **Minecraft** | `26.1.2` |
| **Loader** | NeoForge `26.1.2.64-beta` |
| **Author** | AkiraHane |
| **License** | All Rights Reserved |

## Dependencies

| Dependency | Version | Required |
|---|---|---|
| PlayerAnimationLib | `1.2.3+mc.26.1` | Yes. Because the PlayerAnimationLib repository is occasionally unreachable, the required Core and Neo jars are included directly in `libs/`. |
| MochaFloats | `5.0.0` | Yes. Bundled in `libs/` for the same reason as above — it is a dependency of PlayerAnimationLib. |
| Curios | `15.0.0-beta.2+26.1.2` | Optional (Jet Booster equipment slot) |

## Build

Requires **JDK 25** (Mojang ships Java 25 with Minecraft 26.1).

```bash
./gradlew build        # Build the mod
./gradlew runClient    # Launch client for testing
./gradlew runData      # Generate data/resources
```

## Features

### Core Mechanics

The mod overrides the player's ground and air friction, acceleration, and speed limits when the mod mode is active. All modifications use a composable effect system that processes modifiers in priority order. Each movement action has independent enable/disable toggles in both the server and client configs — both must be `true` for the action to activate.

- **Reduced air drag**: the vanilla hard-coded `0.91` air friction multiplier is replaced with a configurable value.
- **Jump boost scaling**: horizontal sprint-jump speed scales with the Jump Boost effect level, capped by a configurable speed limit.
- **Dynamic water friction**: movement speed in water is adjusted based on the player's submergence ratio — the more of the body is out of water, the closer the friction is to air values.
- **Auto step-down**: when the player is on the ground and the horizontal movement would carry them downhill, the mod detects the slope and automatically steps down. The effective step-down height equals the vanilla auto-step-up height plus the horizontal speed component. Disabled in liquids and when already stepping up.
- **Ladder speed boost**: holding the sprint key on ladders multiplies vertical climb speed.

### Movement Actions

The mod evaluates **17 movement states** each tick, entering the highest-priority matching state. Each state defines its own entry condition, per-tick behavior, and exit cleanup.

#### Ground

| State | Trigger | Behavior |
|---|---|---|
| **Slide** | Player is on ground, moving above a speed threshold, and presses the lower-center key (`C`) | Slides with reduced friction. When moving downhill, the detected slope direction is used as an acceleration vector. On exit: if horizontal speed exceeds vertical speed, may chain into another slide. |
| **Prone** | On ground while holding `C`, or currently in a swimming pose in a space shorter than the crouch height | Forces the swimming pose, allowing the player to crawl through 1-block-high gaps. Body rotation follows horizontal movement direction. |
| **Walk** | On ground, no higher-priority state matched | Applies the mod's ground physics (custom friction, acceleration, speed limits). |

#### Air

| State | Trigger | Behavior |
|---|---|---|
| **Airborne** | Not on ground, no wall or water state matched | In-air movement with mod acceleration, friction, and horizontal speed limit. Tracks per-tick speed for animation and MoLang bindings. |
| **Break Fall Ready** | Falling with downward speed above a threshold while holding `C` | Preparation stance tracked by fall-speed animation. Transitions into Break Fall on landing. |
| **Break Fall** | Takes fall damage while in a lowered posture (slide, prone, or break-fall-ready) | Rolls on landing to reduce fall damage. If forward horizontal speed exceeds downward speed when exiting, chains into a slide. With Jet Booster, fall damage is further reduced. |

#### Wall

| State | Trigger | Behavior |
|---|---|---|
| **Wall Slide** | In the air, moving toward a wall | Drops along the wall with reduced fall speed. Fall damage on landing is reduced. |
| **Wall Hang** | Falling near a wall edge or ledge within detection range | Hangs on the wall edge. While hanging, the player can look sideways and move left/right; body rotation is clamped. |
| **Wall Climb** | Against a wall, moving toward it, holding `Jump` | Climbs up the wall. Climb height scales with jump strength. Jet Booster caps the minimum upward speed. |
| **Wall Kick** | Near a wall while airborne with a wall-jump cooldown available | Launches away from the wall with a fixed horizontal impulse. Has a cooldown. |
| **Wall Run** | Moving parallel to a wall above a horizontal speed threshold, pressing `Up` | Runs along the wall. Body rotates to face the wall. Jet Booster adds an upward velocity component to counter gravity. |
| **Vault Up** | Hanging on a wall edge, pressing `Jump` | Pulls the player up onto the ledge. |
| **Vault In** | Standing or hanging near a 1-block-high gap; while standing press `Up` + `C`, while hanging press `C` + `Jump` | Crawls into the gap using the swimming pose. |

#### Water

| State | Trigger | Behavior |
|---|---|---|
| **Swim** | Sprinting underwater while holding `Up`, or at the water surface while holding `C` + `Up` | Vanilla swimming with mod-adjusted water friction. |
| **Swim Dash** | Underwater, holding `Sprint` (while already swimming) or `Up` + `Sprint` (from below surface) | Dashes forward in the look direction with a 10-tick push timer. If the dash exits the water surface, the swimming pose and body rotation are maintained during the airborne arc (dolphin jump). |

#### Special

| State | Trigger | Behavior |
|---|---|---|
| **Dodge** | Double-tap the sprint key while holding a direction key (configurable to single-tap); Jet Booster enables mid-air dodge | Quick dash in the chosen direction, with brief invincibility frames (10 ticks). Uses shared cooldown with Swim Dash; up to one charge can be stored. |
| **Original** | Mod toggled off (`Shift + M`) | All mod behavior disabled; vanilla movement applies. This is also the fallback when no other state matches. |

### Equipment

**Jet Booster** — equippable in the Curios belt slot. When equipped:
- Movement speed and jump boost are increased via attribute modifiers.
- Wall run does not lose altitude.
- Mid-air dodge is enabled.
- Break Fall damage is reduced further.

### Key Bindings

| Key | Action |
|---|---|
| `C` | Lower center (prone, slide, break-fall-ready, vault-in) |
| `Shift + M` | Toggle mod mode on/off |
| `Shift + N` | Toggle on-screen key hints on/off |

Most active actions display contextual key prompts on screen, showing the relevant vanilla key bindings (`W`, `A`, `S`, `D`, `Space`, `Shift`, `Ctrl`).

### Configuration

All feature flags live in `ServerConfig` and `ClientConfig`. Most actions require both to be enabled:

- `ServerConfig` controls server-side enforcement. Disabling an action here prevents ALL players from using it.
- `ClientConfig` allows individual players to opt out of specific actions locally.

Config values use NeoForge's `ModConfigSpec` system.

## Architecture

- **State machine**: each player has a `MovementStateMachine` that evaluates 17 state transitions per client tick in priority order. State changes are synced to the server via a lightweight packet.
- **Effect system**: physics values (acceleration, friction, block friction, speed limit) are modified by composable effects with types `REPLACE`, `BASE_MULTIPLIER`, `LOCAL_VALUE`, `COMPOSE`, and `MULTIPLIER`, applied in priority order.
- **Physics injection**: 4 Mixin classes modify `Entity` (step-up/step-down/slope detection), `LivingEntity` (air/water travel, jump boost, body rotation, ladder speed), `GameRenderer` (camera roll/FOV), and `AvatarRenderer` (swimming-pose rendering for non-vanilla states).
- **Animation**: PlayerAnimationLib drives bone animations from bedrock-format animation files, with MoLang expressions bound to per-tick movement data exported from `PlayerMovementContext`.

## Feedback

Issues and suggestions are welcome. Include mod version, Minecraft version, and reproduction steps when reporting a problem.

| Platform | Info |
|----------|------|
| QQ | `1796334524` |
| Bilibili | [@AkiraHane](https://space.bilibili.com/27666009) |

---

> This document was generated with AI assistance.
