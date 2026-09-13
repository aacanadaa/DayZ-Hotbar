# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Hotbar** is a Minecraft **1.20.1** client-side mod that replaces the vanilla
HUD with a DayZ-style hotbar and a DayZ-style status readout. Its visual language is
deliberately shared with the sibling **DayZ Inventory** mod — flat translucent
near-black panels, hairline separators, desaturated grey text, no borders or chrome.

- **Group / package root**: `com.suoim.dayzhotbar`
- **Mod ID**: `dayz_hotbar`
- **License**: Apache License 2.0 (see `LICENSE`), matching the sibling DayZ Inventory
  repo. This was PolyForm Noncommercial 1.0.0 up to and including the 1.0.0 release and
  was relicensed to Apache 2.0 before the Forge port shipped.
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

### 5. The Forge port is written but NOT shipped — and not included in the build

`forge/` exists and builds a correct jar. It is deliberately absent from
`settings.gradle`, so `./gradlew build`, CI and releases all ignore it.

Where it got to, so a retry does not repeat the work:

- **Packaging is correct and verified.** Searge refmap (`Gui;m_280518_`), reobfuscated
  classes, `MixinConfigs` in the manifest, and the jar in `build/libs` is the
  reobfuscated one rather than the dev jar. The dev client (`:forge:runClient`) renders
  the HUD correctly and its log shows `Mixing GuiMixin ... into net.minecraft.client.gui.Gui`.
- **The mixin does apply in a real Forge client.** Confirmed by reflecting on
  `net.minecraft.client.gui.Gui` from the mod constructor: all five handlers
  (`dayzHotbar$replaceHotbar`, `replaceStatus`, `hideExperienceBar`, `hideVehicleHealth`,
  `sample`) are merged into the class.
- **The actual symptom:** with the mixin applied, the DayZ hotbar draws and vanilla's
  hotbar is correctly cancelled — but the status readout never appears and vanilla's
  health and food bars are never cancelled. So `renderHotbar` succeeds while
  `renderStatus` does not, from two injections in the same mixin class that share
  identical guards (`hideGui`, null player).

That asymmetry is the thread to pull. The next step was to log from inside
`dayzHotbar$replaceStatus` what it returns and why — not to keep comparing jars, which
is where most of the time went. Comparing the Forge jar against the sibling DayZ
Inventory Forge jar showed no structural difference at all, and two false leads were
chased before that (the `client` vs `mixins` list, and the `MixinConfigs` manifest
attribute) — both were disproved by direct test.

One caution: `:forge:publishMods` reads the reobfuscated jar through a task dependency,
not a path. Reading `jar.archiveFile` is how a broken, un-reobfuscated Forge release
reached Modrinth and CurseForge once already while the GitHub release was fine.

## Design Notes

- **Icons are hand-drawn pixel art, not a texture.** Each one is a 9x9 character grid
  in `Icons` where `#` is a cell; it is drawn as 2px rectangles and clipped to the fill
  fraction. If you add one, keep every row exactly 9 characters — the renderer does not
  pad, a short row just draws fewer cells.
  - Shapes are drawn **solid**, which means fine detail is lost: a one-cell notch
    disappears and the icon reads as a blob. The heart's lobe notch is three cells
    wide for exactly this reason, and the apple narrows to its stem so it does not
    read as an egg.
  - There is no outline pass. `ICON_EMPTY` (the un-filled remainder) has to stay faint
    or the empty part of a shape looks solid and the fill level stops reading.
- **Colour bands live in `Stat.tierColor`.** They are fractions, not absolute values,
  so they apply to health, food, armour and air alike. Absorption opts out via
  `Stat.tiered()` because having less of it is not a warning.
- **The trend chevron is a rank insignia, not an arrow** — 9x5 cells at 2px, which is
  18x10 with four-pixel arms. A 1px arrow is invisible next to 18px icons. It is always
  white; direction is the orientation, not the colour.


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
