# Momentum

[中文版本](README_CN.md)

A Fabric / NeoForge mod that adds parkour actions such as sliding, wall climbing, wall running, and dodging to Minecraft. Maneuver mode also adjusts movement friction, acceleration, and speed limits, with automatic step-down and faster ladder climbing while holding sprint.

## Installation

This branch targets Minecraft `26.1.2` with Fabric Loader `0.19.2` or NeoForge `26.1.2.64-beta`.

Place the mod and required dependencies for your loader in `mods/`:

| Platform | Dependency | Version | Purpose |
|---|---|---|---|
| Both | Player Animation Library (matching loader edition) | `1.2.8+mc.26.1` | Required for player animations |
| Fabric | Fabric API | `0.149.1+26.1.2` | Required platform APIs |
| NeoForge | Curios | `15.0.0-beta.2+26.1.2` | Optional belt slot for the Jet Booster |
| Fabric | Trinkets Updated | `4.0.1+26.1` | Optional belt slot for the Jet Booster |

This version of PlayerAnimationLib includes MochaFloats.

## Controls

The bindings below are defaults. Forward, jump, sneak, and sprint use your Minecraft key bindings. On-screen hints show available actions.

| Key | Action |
|---|---|
| `C` | Lower your center of gravity |
| `Shift + M` | Toggle maneuver mode |
| `Shift + N` | Toggle key hints |

On Fabric, the action keys are bound to `M` and `N`, with a separate Function Modifier bound to Left Shift by default. It applies only to these two toggles. Press the modifier and action key in either order; releasing and pressing the modifier again while holding the action key triggers another toggle. Unbind the modifier in Key Binds to use `M` and `N` alone. `C` and vanilla movement keys remain independent. Left Shift also retains its vanilla sneak action.

NeoForge continues to use its own modifier settings.

| Movement | Controls and effect |
|---|---|
| Prone | Hold `C` on the ground to crawl through 1-block-high gaps |
| Slide | Hold `C` while moving fast enough on the ground |
| Power Jump | Release sneak on the ground and immediately jump for extra height |
| Break Fall | Hold `C` while falling to roll on landing and reduce fall damage |
| Wall Climb | Move toward a wall while holding jump |
| Wall Slide | Face a nearby wall while falling, then move toward it or hold jump to slow your descent |
| Wall Hang | Automatically grab a reachable ledge while falling; press sneak to let go |
| Wall Kick | Move away from a wall while airborne and press jump; during a wall run, just press jump to kick off |
| Wall Run | Move along a wall fast enough while airborne and hold forward + jump; keep holding forward to continue |
| Vault Up | Press jump while holding a ledge to get onto the platform |
| Vault In | Face a low gap; press forward + `C` while standing, or `C` + jump while hanging |
| Swim | Hold forward + sprint underwater, or forward + `C` at the surface |
| Swim Dash | Press sprint underwater to dash in your look direction; also hold forward if you are not already swimming |
| Dodge | Hold sprint (default `Ctrl`) and double-tap a direction key to dodge in that direction |

Dodge and Swim Dash share a cooldown. Dodge controls can be changed in the client config.

## Jet Booster

Equip it in the leggings slot, or the belt slot with Curios (NeoForge) or Trinkets Updated (Fabric) installed. It increases movement speed, jump height, and step height, enables mid-air dodging, slows descent during wall runs, and reduces fall damage.

## Configuration

- Server config: movement toggles, movement parameters, cooldowns, and booster requirements.
- Client config: movement toggles, dodge controls, camera effects, and key hints.

Most actions require the corresponding toggles in both configs to be enabled.

## Building

Requires JDK 25. Gradle retrieves dependencies from Maven repositories.

| Command | Purpose |
|---|---|
| `./gradlew build` | Build both loaders; artifacts are in each module's `build/libs/` |
| `./gradlew :fabric:runClient` | Launch the Fabric development client |
| `./gradlew :neoforge:runClient` | Launch the NeoForge development client |
| `./gradlew :neoforge:runData` | Run NeoForge data generation |

In Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

## Feedback

Include the mod version, Minecraft version, and reproduction steps when submitting an issue.

- QQ: `1796334524`
- Bilibili: [@AkiraHane](https://space.bilibili.com/27666009)

See [LICENSE](LICENSE) for licensing.
