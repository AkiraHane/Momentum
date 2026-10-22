# Momentum

[中文版本](README_CN.md)

A Minecraft parkour mod with sliding, wall running, ledge hanging, wall kicks, and dodging. Supports Fabric and NeoForge.

## Installation

This branch targets Minecraft `26.1.2` with Fabric Loader `0.19.2` or NeoForge `26.1.2.64-beta`.

Place the mod and required dependencies for your loader in `mods/`:

| Loader | Dependency | Version | Notes |
|---|---|---|---|
| Fabric / NeoForge | Player Animation Library (matching loader edition) | `1.2.8+mc.26.1` | Required |
| Fabric | Fabric API | `0.149.1+26.1.2` | Required |
| NeoForge | Curios | `15.0.0-beta.2+26.1.2` | Optional belt slot for the Jet Booster |
| Fabric | Trinkets Updated | `4.0.1+26.1` | Optional belt slot for the Jet Booster |

## Controls

The bindings below are defaults. Forward, jump, sneak, and sprint use your Minecraft key bindings. On-screen hints show available actions.

| Key | Action |
|---|---|
| `C` | Lower your center of gravity |
| `Shift + M` | Toggle maneuver mode |
| `Shift + N` | Toggle key hints |
| Unbound | Wall interaction |

On Fabric, a separate Function Modifier (Left Shift by default) is used with `M` and `N`. Unbind it to use `M` and `N` alone.

If you bind Wall Interaction, you must hold it to wall run, climb, slide, or hang; release it to let go. Leave it unbound for automatic wall interaction.

| Movement | Controls and effect |
|---|---|
| Prone | Hold `C` on the ground to crawl through 1-block-high gaps |
| Slide | Hold `C` while moving fast enough on the ground to slide through 1-block-high gaps |
| Power Jump | Release sneak on the ground and immediately jump for extra height |
| Break Fall | Hold `C` while falling to roll on landing and reduce fall damage |
| Wall Climb | Move toward a wall while holding jump to climb a short distance up the wall |
| Wall Slide | Face a nearby wall while falling, then move toward it or hold jump to slow your descent |
| Wall Hang | Automatically grab a reachable ledge while falling; press sneak to let go |
| Wall Kick | Move away from a wall while airborne and press jump, or press jump during a wall run. Looking up or down affects the vertical direction of the jump |
| Wall Run | Move along a wall fast enough while airborne and hold forward + jump; keep holding forward to continue |
| Vault Up | Press jump while holding a ledge to get onto the platform |
| Vault In | Face a 1-block-high gap; press forward + `C` while standing, or `C` + jump while hanging to enter it |
| Swim | Hold forward + `C` in water to start swimming |
| Swim Dash | Press sprint underwater to dash in your look direction; also hold forward if you are not already swimming |
| Dodge | Hold sprint (default `Ctrl`) and double-tap a direction key to dodge in that direction |

Dodge and Swim Dash share a cooldown. Dodge controls can be changed in the client config.

## Jet Booster

Equip it in the leggings slot, or the belt slot with Curios (NeoForge) or Trinkets Updated (Fabric) installed. It increases movement speed, jump height, and step height, enables mid-air dodging, slows descent during wall runs, and reduces fall damage.

## Configuration

- Server config: movement toggles, movement parameters, cooldowns, and booster requirements.
- Client config: movement toggles, dodge activation methods, camera effects, and key hints.

Most actions require the corresponding toggles in both configs to be enabled.

## Building

Requires JDK 25.

| Command | Purpose |
|---|---|
| `./gradlew build` | Build Fabric and NeoForge versions; artifacts are in each module's `build/libs/` |
| `./gradlew :fabric:runClient` | Launch the Fabric development client |
| `./gradlew :neoforge:runClient` | Launch the NeoForge development client |
| `./gradlew :neoforge:runData` | Run NeoForge data generation |

In Windows PowerShell, use `.\gradlew.bat` instead of `./gradlew`.

## Feedback

Include the mod version, Minecraft version, and reproduction steps when submitting an issue.
If you haven't received a response for a while, you can also contact the author through:

- QQ: `1796334524`
- BiliBili: [@银芽Linqi](https://space.bilibili.com/27666009)
- X: [银芽](https://x.com/LinqiYinya)

See [LICENSE](LICENSE) for licensing.
