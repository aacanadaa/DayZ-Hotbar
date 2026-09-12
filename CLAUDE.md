# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Hotbar** is a Minecraft **1.20.1** client-side mod that replaces the vanilla
HUD with a DayZ-style hotbar and a DayZ-style status readout. Its visual language is
deliberately shared with the sibling **DayZ Inventory** mod — flat translucent
near-black panels, hairline separators, desaturated grey text, no borders or chrome.

- **Group / package root**: `com.suoim.dayzhotbar`
- **Mod ID**: `dayz_hotbar`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Environment**: client only

## Repository Layout

```
common/    All HUD logic and the Gui mixin. Loader-agnostic.
fabric/    Fabric entrypoint + fabric.mod.json
```

`common/` uses **fabric-loom** for its Minecraft dependency but its code never imports
`net.fabricmc.*` outside the loader entrypoint. It is written so a Forge or NeoForge
module can be added by copying `fabric/` and swapping the entrypoint.

## Build

Requires **JDK 17**. Do not hardcode `org.gradle.java.home` in `gradle.properties`.
Set `JAVA_HOME` instead:

```bash
JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64 ./gradlew build
```

Output: `fabric/build/libs/dayz-hotbar-fabric-1.20.1-<version>.jar`

## Critical Gotchas

### 1. Minecraft 1.20.1's `Gui` API is not the API you remember

These were all verified against the real mapped jar, not from memory. Getting any of
them wrong produces a mixin that silently does not apply.

- **`renderHotbar` takes the partial tick FIRST**: `renderHotbar(float, GuiGraphics)`.
  Every other render method here takes `GuiGraphics` first. This one is reversed and is
  the single easiest thing to get wrong.
- **`renderPlayerHealth(GuiGraphics)` draws health, food, armour AND air.** In 1.20.1
  there is no `renderFood`, `renderArmor` or `renderAir` — they were all inlined into
  that one method. They cannot be replaced individually.
- **`renderExperienceBar(GuiGraphics, int)` draws the bar *and* the level number.**
  There is no `renderExperienceLevel`.
- **There is no `blitSprite`** — that arrived in 1.20.2. Partial fills are done with
  `blit(...)` plus `enableScissor`/`disableScissor`.
- **`Inventory.selected` is a plain public field.** `getSelectedSlot`/`setSelectedSlot`
  do not exist until much later versions.
- **`Gui.GuiGraphics` is not a field.** `GuiGraphics` is created per frame by
  `GameRenderer` and passed down as a parameter. Do not `@Shadow` it.
- `Gui.HeartType.POISIONED` is genuinely misspelled in Mojang's 1.20.1 mappings.

### 2. Verify the built jar, not the build

A green build and a working dev client do not mean the released jar works. Before any
release, open the jar and confirm:

- `dayz-hotbar.refmap.json` exists and its `data` block is `named:intermediary`
- every injected method in the refmap resolved to a real target (a name that did not
  resolve is simply absent, so a missing entry is the symptom)
- `@Mixin(Gui.class)` was remapped — the class constant pool must contain
  `net/minecraft/class_329` and **zero** references to `net/minecraft/client/gui/Gui`.
  If the annotation is not remapped the mixin cannot resolve in production, and
  because the config is `required: true` the game **crashes** rather than degrading.
- the jar is a sane size (this mod is roughly 26 KB; anything in the hundreds of bytes
  is an empty jar)

### 3. Fabric dev runs do not apply mixins in this project

`./gradlew :fabric:runClient` starts the game, but `GuiMixin` does not apply, so the
vanilla HUD is **not** replaced. This is a consequence of using
`loom.officialMojangMappings()` — Loom writes the refmap in the production namespace
and Mixin then looks for `class_329` in a development environment that uses official
names. It is not a regression to "fix" casually.

**Do not use `:fabric:runClient` to judge whether the HUD looks right.** Install the
built jar into a real Fabric installation instead.

### 4. The refmap name is forced in three places

The mixin config declares `"refmap": "dayz-hotbar.refmap.json"`. Loom would otherwise
name it after the project directory (`common-refmap.json`), which nothing references.
The name is kept in sync by `loom.mixin.defaultRefmapName` in `common/build.gradle`,
and both `processResources` and `jar` in `fabric/build.gradle` exclude a stale
`common-refmap.json` so it can never shadow the correct one.

## Design Notes

- **All rendering is procedural.** Every panel, slot, outline and chevron is drawn with
  `GuiGraphics.fill`; the only texture used is vanilla's own `textures/gui/icons.png`
  for the status icons. No new assets ship with the mod beyond the icon.
- **`HudTheme` mirrors DayZ Inventory's constants.** If one mod's palette changes, the
  other should follow.
- **Vanilla HUD elements are suppressed, not drawn over**, by cancelling each element
  and drawing the replacement at that exact point in the render order. Drawing
  everything at the end of `Gui#render` instead would put the hotbar on top of chat,
  the tab list and the scoreboard.
- **Trend velocities are sampled once per tick**, detected from `Gui#getGuiTicks`. The
  `Gui#render` injection that samples also resets the history across a gap of more than
  two ticks, so a paused game or an open screen cannot produce a phantom velocity spike.
