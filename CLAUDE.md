# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Hotbar** is a Minecraft **1.21.1** client-side mod that replaces the vanilla
HUD with a DayZ-style hotbar and a DayZ-style status readout. Its visual language is
deliberately shared with the sibling **DayZ Inventory** mod — flat translucent
near-black panels, hairline separators, desaturated grey text, no borders or chrome.

- **Group / package root**: `com.suoim.dayzhotbar`
- **Mod ID**: `dayz_hotbar`
- **License**: Apache License 2.0 (see `LICENSE`), matching the sibling DayZ Inventory
  repo. This was PolyForm Noncommercial 1.0.0 up to and including the 1.0.0 release and
  was relicensed to Apache 2.0 before the Forge port shipped.
- **Environment**: client only
- **Supported Minecraft versions**: this tree builds **1.21.1** only. 1.20.1 is a
  still-supported release point rather than a module here — the source was ported forward
  rather than branched, so the 1.20.1 code is the **`v1.0.1`** tag in this repo's history
  (Fabric and Forge only, JDK 17). Anything that must hold for both versions has to be
  changed twice, at two different points in history; there is no shared branch. The
  user-facing version matrix lives in `README.md` and both store descriptions.

## Documentation Language

The repository docs are **bilingual, and Chinese leads**. `README.md` is the Chinese page
and the one GitHub shows by default; `README.en.md` is the English translation. Both open
with a cross-link line (`**中文** | [English](README.en.md)` and
`[中文](README.md) | **English**`), matching the sibling DayZ Inventory repo.

- Keep the two pages in step. A feature, version, filename or link added to one belongs in
  the other in the same change.
- Translate prose only. Badge URLs, code spans, jar names, file paths, version numbers,
  tables and links stay identical in both files; the sole deliberate difference is the
  Ko-fi badge label (`Support me` in English, `赞助我` in Chinese).
- A doc-only change does not bump `mod.version`. Record it in `CHANGELOG.md` under the
  release it lands in, as the 1.1.0 entry does.

## Repository Layout

```
common/    All HUD logic and the Gui mixin. Loader-agnostic.
fabric/    Fabric entrypoint + fabric.mod.json
forge/     Forge entrypoint + mods.toml + the layer event handler
neoforge/  NeoForge entrypoint + neoforge.mods.toml + the layer event handler
```

`common/` uses **fabric-loom** for its Minecraft dependency but its code never imports
`net.fabricmc.*` outside the loader entrypoint. Both loader modules compile its sources
directly into their own output rather than depending on it as a project, so there is one
copy of the HUD implementation and no cross-loader artifact to publish.

A loader module is **not** a copy of `fabric/` with a swapped entrypoint. Each loader
reaches the HUD by its own mechanism — Fabric mixes into `Gui`, Forge and NeoForge each
hook their own layer renderer — so a new module needs its own plumbing rather than a
renamed entrypoint. See gotcha 5.

Forge and NeoForge are **not** one module with two manifests either. They are separate
products from the same fork and their HUD APIs are genuinely different classes in
different packages (see gotcha 5), so each needs its own handler.

## Build

Requires **JDK 21** — Minecraft 1.21.1 is a Java 21 target, and a 17 toolchain cannot
compile it. Do not hardcode `org.gradle.java.home` in `gradle.properties`. Set
`JAVA_HOME` instead:

```bash
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew build
```

Outputs:

- `fabric/build/libs/dayz-hotbar-fabric-1.21.1-<version>.jar`
- `forge/build/libs/dayz-hotbar-forge-1.21.1-<version>.jar`
- `neoforge/build/libs/dayz-hotbar-neoforge-1.21.1-<version>.jar`

All three are plain `jar` output and all three are directly shippable. See gotcha 6 for
why the Forge module no longer has a reobfuscation step — that is a change from 1.20.1,
where the released Forge jar was the output of a separate `reobfuscatedJar` task and the
`jar` task wrote to `build/devlibs`.

Every module also produces a `-sources.jar` in the same directory as its real artifact,
because the root build enables `withSourcesJar`. Any glob that collects release jars has
to exclude them — see the CI workflow.

## Critical Gotchas

### 1. Minecraft 1.21.1's `Gui` API is not the API you remember

These were all verified against the real mapped 1.21.1 jar, not from memory. Getting any
of them wrong produces a mixin that silently does not apply.

- **The partial tick is gone; it is a `DeltaTracker` now.** `render` is
  `render(GuiGraphics, DeltaTracker)`. Every method that used to take a `float
  partialTick` takes a `DeltaTracker` in its place, and `getGuiTicks()` still exists
  and is what the HUD samples on.
- **`renderHotbar` does not exist.** The slot row is `renderItemHotbar(GuiGraphics,
  DeltaTracker)`. Note that the argument order also flipped: 1.20.1's reversed
  `(float, GuiGraphics)` is now the ordinary `(GuiGraphics, …)`.
- **`renderPlayerHealth(GuiGraphics)` still draws health, food, armour AND air.** This
  one did *not* split, even though 1.21.1 wraps it in a layered renderer and NeoForge
  splits it further on its own. In vanilla there is still no `renderFood` you can cancel
  separately — `renderFood` and `renderArmor` exist but are private helpers called from
  inside it.
- **The experience level is its own method now**: `renderExperienceLevel(GuiGraphics,
  DeltaTracker)`. `renderExperienceBar(GuiGraphics, int)` draws only the bar, so
  cancelling the bar alone leaves the level number floating in mid-air.
- **Vanilla builds its HUD as a `LayeredDraw`** (`net.minecraft.client.gui.LayeredDraw`,
  a `ResourceLocation`-keyed list of `LayeredDraw.Layer`, each
  `render(GuiGraphics, DeltaTracker)`). This arrived in 1.21, not 1.21.2, and it is why
  both Forge and NeoForge were able to drop their old overlay systems. See gotcha 5.
- **`Inventory.selected` is still a plain public field.** `getSelectedSlot`/
  `setSelectedSlot` do not exist even here.
- **`Gui.GuiGraphics` is not a field.** `GuiGraphics` is created per frame by
  `GameRenderer` and passed down as a parameter. Do not `@Shadow` it.
- **Effects are `Holder<MobEffect>`.** `MobEffects.REGENERATION` is a
  `Holder<MobEffect>` and `MobEffectInstance.getEffect()` returns one. Comparing them
  with `Holder#is(Holder)` is **deprecated** — two holders can wrap the same effect and
  still not be the same holder. Compare `effect.value()` instead, as `EffectGroup` does.

### 2. Verify the built jar, not the build

A green build and a working dev client do not mean the released jar works. Before any
release, open the jar and confirm:

**The Fabric jar** — this is the only one with a mixin, and it is the only one that can
fail this way:

- `dayz-hotbar.refmap.json` exists and its `data` block is `named:intermediary`
- every injected method in the refmap resolved to a real target (a name that did not
  resolve is simply absent, so a missing entry is the symptom). On 1.21.1 the five
  injections plus the sampler must all be present:

  | mixin method | intermediary target |
  | --- | --- |
  | `render` | `method_1753` |
  | `renderItemHotbar` | `method_1759` |
  | `renderPlayerHealth` | `method_1760` |
  | `renderExperienceBar` | `method_1754` |
  | `renderExperienceLevel` | `method_56136` |
  | `renderVehicleHealth` | `method_1741` |

- `@Mixin(Gui.class)` was remapped — the class constant pool must contain
  `net/minecraft/class_329` and **zero** references to `net/minecraft/client/gui/Gui`.
  If the annotation is not remapped the mixin cannot resolve in production, and
  because the config is `required: true` the game **crashes** rather than degrading.
- `common-refmap.json` is absent (see gotcha 4)

**The Forge and NeoForge jars** have no mixin at all and must **not** contain
`GuiMixin.class`, any `*refmap.json`, or `dayz-hotbar.client.mixins.json`. Their HUD
goes through loader APIs, so there is no namespace remapping left to get wrong — which
is exactly why they were built that way. Confirm instead that the metadata is right:
`META-INF/mods.toml` for Forge, `META-INF/neoforge.mods.toml` for NeoForge, and
`pack.mcmeta` in both.

**All three**: the jar is a sane size — roughly 100 KB, of which about 65 KB is
`icon.png`. Anything in the hundreds of bytes is an empty jar. A `-sources.jar` of
about 2.8 KB sits next to each one and is **not** the mod.

### 3. Judge the HUD from an installed jar, not from `runClient`

On 1.20.1 this was a hard fact: `./gradlew :fabric:runClient` started the game but
`GuiMixin` did not apply, so the vanilla HUD was **not** replaced. The cause was
`loom.officialMojangMappings()` — Loom writes the refmap in the production namespace and
Mixin then looked for `class_329` in a development environment using official names.

**That specific failure has not been re-tested on 1.21.1**, so do not assume it either
way. The guidance does not depend on it:

**Do not use `:fabric:runClient` to judge whether the HUD looks right.** Install the
built jar into a real Fabric installation — or a launcher instance, which is what the
`1.21.1 fabric tester` / `1.21.1 forge tester` / `1.21.1 neoforge tester` instances
under Prism are for — and check there. A dev run answers a different question from the
one you are asking, and if the mixin silently does not apply it answers it *quietly*.

### 4. The refmap name is forced in three places

The mixin config declares `"refmap": "dayz-hotbar.refmap.json"`. Loom would otherwise
name it after the project directory (`common-refmap.json`), which nothing references.
The name is kept in sync by `loom.mixin.defaultRefmapName` in `common/build.gradle`,
and both `processResources` and `jar` in `fabric/build.gradle` exclude a stale
`common-refmap.json` so it can never shadow the correct one.

### 5. Forge and NeoForge drive the HUD through their own layer renderers — the shared mixin applies to neither

Both modules deliberately do **not** use `GuiMixin`, and on 1.21.1 the reason has
changed completely from 1.20.1. The old story — `ForgeGui extends Gui` bypassing
`Gui.render`, one overlay per vanilla element, a mixin that resolved and applied but was
never invoked — is **gone**. It is not worth re-litigating; none of those classes exist.

What replaced it:

- **Vanilla 1.21.1 builds its HUD as a `LayeredDraw`** (see gotcha 1). Both forks kept
  that and hung their own API off it, which is why both could drop their overlay systems
  in one go.
- **Forge has no `ForgeGui`, no `GuiOverlayManager`, no `IGuiOverlay`, no
  `VanillaGuiOverlay` and no `RenderGuiOverlayEvent`.** `Gui` now holds a
  `net.minecraftforge.client.gui.overlay.ForgeLayeredDraw` — a `LayeredDraw` subclass
  that names its layers — and the supported hook is `AddGuiOverlayLayersEvent`, fired
  from `ForgeLayeredDraw.resolveLayers()` at the end of `Gui`'s constructor with the
  fully built tree. There is no render-time event: layers are switched off by adding a
  condition, not by cancelling at draw time.
- **NeoForge is the same idea with a different spelling**, because it forked before
  Forge wrote any of this. Its `Gui` holds a
  `net.neoforged.neoforge.client.gui.GuiLayerManager`, its hook is the *render-time*
  `RenderGuiLayerEvent.Pre` (cancellable, per named layer, and it fires for nested layer
  groups too), and `VanillaGuiLayers` carries the ids.

Three things that burn time if forgotten:

- **Forge's layer names are not NeoForge's.** Forge groups the whole bottom-left block —
  slot row, experience bar, health row *and* mount health — into one layer called
  `ForgeLayeredDraw.HOTBAR`, because it never split `renderHotbarAndDecorations`. So on
  Forge there is nothing finer to switch off, and the brief "selected item name" popup
  goes with it. NeoForge splits all of it: `HOTBAR` is only the slots, and
  `PLAYER_HEALTH`, `ARMOR_LEVEL`, `FOOD_LEVEL`, `AIR_LEVEL`, `VEHICLE_HEALTH` and
  `EXPERIENCE_BAR` are each their own layer.
- **Forge's replaced layers are nested, NeoForge's are too but differently.** On Forge,
  `HOTBAR` and `EXPERIENCE` live inside the `PRE_SLEEP_STACK` sub-stack, so
  `addConditionTo` has to be given the stack as well as the layer. Addressing the root
  finds nothing and only logs a warning.
- **Both modules compile the common sources with the mixin excluded**, so neither ships
  `GuiMixin.class`, a refmap, or a mixin config. Neither declares a Mixin dependency.

NeoForge's replaced layers are cancelled **unconditionally**, not only when the mod drew
something: NeoForge gates those layers on its own `hideGui` and survival-mode
conditions, so cancelling only on a successful draw would leave vanilla's health and
food rows visible with F1 pressed. The mod's own layer is gated on `!hideGui` instead,
which is what Fabric gets for free by never reaching those methods while the HUD is
hidden.

### 6. Forge 1.21.1 does not reobfuscate — do not reintroduce a reobf step

On 1.20.1 the Forge jar had to be reobfuscated: Forge resolved members against SRG at
runtime while `jar` packaged official (Mojang) names, so ForgeGradle rewrote the jar in
place, `jar` was redirected to `build/devlibs`, and a `reobfuscatedJar` task copied the
rewritten file into `build/libs`. Getting that wrong once shipped a broken Forge release.

**On 1.21.1 there is no reobfuscation.** Forge resolves against official names at
runtime, exactly like NeoForge. This was verified by scanning all 1201 classes in
`forge-1.21.1-52.1.16-universal.jar` for SRG method references — there are none. There
is consequently **no `reobfJar` task**, and `forge/build.gradle` naming one fails the
build with "Task with name 'reobfJar' not found".

So `forge/build.gradle` publishes `jar.archiveFile` directly. The old failure mode — a
release picking up an un-reobfuscated development jar — cannot recur, because there is no
longer a second copy of the jar to pick up by mistake. If a future Minecraft version
reintroduces SRG, this section is the one to revisit.

### 7. Compile against the OLDEST loader version you declare, not the newest

This one has already bitten once, on the 1.21.1 port, and it is worth understanding
because every check the project has would have passed.

The Forge module was compiled against **52.1.16** while `mods.toml` declared
`versionRange="[52,)"`. Forge 1.21.1 had no HUD API at all until **52.1.2** — no
`ForgeGui`, no `RenderGuiOverlayEvent`, and no `ForgeLayeredDraw` either, so there is
nothing to hook on 52.1.0 or 52.1.1. A user on 52.1.0 therefore loaded the mod, and it
died during event-subscriber registration with

```
java.lang.NoClassDefFoundError: net/minecraftforge/client/event/AddGuiOverlayLayersEvent
```

The build was green. The refmap resolved. The jar contained the right classes, the right
metadata and no mixin. **Nothing in the repository could have caught it**, because the
compiler was pointed at a version that has the class and the range admitted versions that
do not.

`forge_version` in `gradle.properties` is therefore pinned to the **declared minimum**
(52.1.2), so `javac` enforces the range instead of trusting it. Keep the two in step: if
the `forge` versionRange in `mods.toml` moves, `forge_version` moves with it.

The same audit for the other two loaders, done at the same time:

- **NeoForge — safe.** `RenderGuiLayerEvent`, `VanillaGuiLayers` and `GuiLayerManager`
  all exist from 21.1.1, and all eight `VanillaGuiLayers` constants the handler uses are
  present there too, so the declared `[21.1,)` is honest.
- **Fabric — safe.** The only loader API used is `ClientModInitializer`, which is
  ancient. The mixin targets come from the Minecraft version, not the loader.

When adding a loader dependency, the check to run is: take the lowest version the
metadata admits, and confirm every class, method and constant you touch exists *there* —
not just in the newest build. `javap -cp <the-old-universal.jar>` is enough.

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
- **Vanilla HUD elements are suppressed, not drawn over**, by switching each element off
  and drawing the replacement at that exact point in the render order. Drawing everything
  at the end instead would put the hotbar on top of chat, the tab list and the
  scoreboard.
- **Trend velocities are sampled once per tick**, detected from `Gui#getGuiTicks`. The
  sampler also resets the history across a gap of more than two ticks, so a paused game
  or an open screen cannot produce a phantom velocity spike.
  - Where the sampler runs differs per loader, but it always runs **before the readout
    that consumes it** in the same frame, which is the only ordering that matters:
    Fabric injects at the head of `Gui#render`, NeoForge subscribes to
    `RenderGuiEvent.Pre` (posted ahead of the layer loop), and Forge samples at the top
    of its own layer, immediately before it draws. `onFrame` is idempotent within a
    tick, so the extra calls a nested layer group produces are harmless.
  - Forge's layer is gated on `!hideGui`, so sampling pauses while the HUD is hidden.
    That is safe only because of the gap reset above — without it, unhiding the HUD
    would read the whole hidden interval as one enormous velocity.
