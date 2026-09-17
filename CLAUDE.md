# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Hotbar** is a client-only Minecraft mod that replaces the vanilla HUD with a
DayZ-style hotbar and a DayZ-style status readout. Its visual language is shared with the
sibling **DayZ Inventory** mod — flat translucent near-black panels, hairline separators,
desaturated grey text, no borders or chrome.

- **Group / package root**: `com.suoim.dayzhotbar`
- **Mod ID**: `dayz_hotbar`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Environment**: client only
- **Modrinth** `hsTmy4Hi` / slug `dayz-hotbar`, **CurseForge** project `1693963`

### One source tree, twenty-three Minecraft versions

This is a **Stonecutter** project (branched layout). There is no branch per Minecraft
version. The enabled matrix is **23 Minecraft versions from 1.20.1 to 26.3**, producing
**50 artifacts** — 23 Fabric, 9 Forge and 18 NeoForge — with version differences marked
inline using `//? if` comments. The 1.20.1 code used to live only in the `v1.0.1` tag; it
is now a node like any other.

| Minecraft | Fabric | Forge | NeoForge | Java |
| :--- | :---: | :---: | :---: | :---: |
| 1.20.1 – 1.20.4 | ✅ | — | — | 17 |
| 1.20.5 | ✅ | — | — | 21 |
| 1.20.6 – 1.21.1 | ✅ | ✅ | ✅ | 21 |
| 1.21.2 | ✅ | — | ✅ | 21 |
| 1.21.3 – 1.21.5 | ✅ | ✅ | ✅ | 21 |
| 1.21.6 – 1.21.7 | ✅ | — | ✅ | 21 |
| 1.21.8 – 1.21.11 | ✅ | ✅ | ✅ | 21 |
| 26.1 – 26.3 | ✅ | — | ✅ | 25 |

`./gradlew matrix` prints the authoritative node list; `versions/<mc>/gradle.properties` is
the only place a version's coordinates live. Do not go below **1.20.1**.

**Read [docs/BUILDING.en.md](docs/BUILDING.en.md) first.** It covers the project tree, the
convention plugins, conditional compilation, the per-version HUD API boundaries and the
publishing setup. The rest of this file is the "things that have actually broken here" list.

## Repository Layout

```
settings.gradle.kts          the version matrix + the Stonecutter tree
stonecutter.gradle.kts       controller: active version, chiseledBuild, publishAll, matrix
gradle.properties            mod metadata, publishing ids, shared tool versions
build-logic/                 convention plugins: dayz-common and dayz-loader
versions/<mc>/gradle.properties   one file per Minecraft version - its only coordinates
common/     Loader-agnostic HUD code + the Fabric GuiMixin
fabric/     Fabric entrypoint (the mixin lives in common)
forge/      Forge entrypoint + ForgeHudHandler
neoforge/   NeoForge entrypoint + NeoForgeHudHandler
```

`<branch>/versions/<mc>/` are build directories; they are gitignored and regenerated.

### Every loader compiles `common`'s processed sources

`dayz-loader` adds the `:common:<mc>` node's **generated** source tree to each loader's
compile task, so common's classes end up in the loader jar. That keeps a single copy of
every class — including the shared mixin — on the mod classpath.

Because of that, `common` must **not** be added as a project dependency. Adding
`implementation project(':common')` puts a second copy of every class on the classpath and,
below 26.1, brings Loom's intermediary refmap that NeoForge and Forge cannot use.

It has to be the *generated* tree and not the raw `common/src`: the raw tree still contains
every version's conditional branches, and a Fabric refmap can only be produced from
annotated sources.

### The Fabric mixin is excluded from Forge and NeoForge

The shared `GuiMixin` is Fabric's mechanism. Forge and NeoForge reach the HUD through their
own layer renderers, so `dayz-loader` excludes `com/suoim/dayzhotbar/client/mixin/**` and the
mixin config from those two branches. Do not "restore" it there: loading it would draw the
HUD twice next to the layer handler.

## Build

The launcher needs **JDK 25**. The Java 17 / 21 / 25 toolchains the other nodes need are
downloaded by the foojay resolver from `versions/<mc>/gradle.properties`. Do not hardcode
`org.gradle.java.home`; it is machine-specific and breaks CI. Set `JAVA_HOME` instead. The
Gradle wrapper is **9.7.0** — Loom 1.18.1 publishes
`org.gradle.plugin.api-version = 9.7.0`.

**There is no bare `./gradlew build`** — `build` only exists per node.

```bash
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew chiseledBuild   # whole matrix
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :fabric:1.21.1:build
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :neoforge:26.2:build
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :forge:1.21.11:build
./gradlew matrix                    # list the nodes
./gradlew :fabric:26.2:runClient    # dev client for one node
```

Output JARs live under the node, not the module:

| Loader | Path |
| :--- | :--- |
| Fabric | `fabric/versions/<mc>/build/libs/dayz-hotbar-fabric-<mc>-<version>.jar` |
| NeoForge | `neoforge/versions/<mc>/build/libs/dayz-hotbar-neoforge-<mc>-<version>.jar` |
| Forge | `forge/versions/<mc>/build/libs/dayz-hotbar-forge-<mc>-<version>.jar` |

## Critical Gotchas

1. **`gradlew` must stay executable** (mode `100755`).
2. **Replacements are bidirectional.** `sc.replacements.string(cond) { replace(a, b) }`
   substitutes `a`→`b` when the condition holds and `b`→`a` when it does not. Neither string
   may be a substring of the other side's content. `MobEffects.HEAL` is a prefix of
   `HEALTH_BOOST`, so anchor such names to `.value()` or a trailing comma.
3. **Do not put comments inside a `//? if` block** — the processor rewrites comment markers
   and a surviving `//` can turn a comment into code.
4. **A mixin whose target has been renamed does not degrade, it crashes.** The client mixin
   config is `required: true`. When a target moves, grep for the call sites and hook the
   method everything routes through; do not guess.
5. **The HUD changed shape across versions, and the boundaries were read off the real jars,
   not guessed.** The mixin and the two layer handlers are organised by era. See
   docs/BUILDING.en.md section 6 for the table.
6. **1.21.6+ moved Forge/NeoForge layer structures** (contextual bar replaces the experience
   bar) and Forge moved to EventBus 7. The import block and the layer constants are
   conditional; do not collapse them.
7. **26.x is unobfuscated.** No mappings, no refmaps, no remap step; `GuiGraphics` is
   `GuiGraphicsExtractor` and `render*` is `extract*`; the HUD lives on `Hud`, not `Gui`.
8. **Never commit secrets.** Publishing tokens come from CI secrets
   (`MODRINTH_TOKEN`/`MODRINTH_PAT`, `CURSEFORGE_API_KEY`/`CURSEFORGE_TOKEN`), never from
   `gradle.properties`.

## Design Notes

- **Icons are hand-drawn pixel art, not a texture.** Each is a grid in `Icons` where `#` is
  a cell; drawn as filled rectangles and clipped to the fill fraction. Keep every row the
  same length — the renderer does not pad.
- **All rendering is procedural.** Every panel, slot, outline and chevron is drawn with
  `fill`; the only texture used is vanilla's own `textures/gui/icons.png`.
- **`HudTheme` mirrors DayZ Inventory's constants.** If one mod's palette changes, the other
  should follow.
- **Vanilla HUD elements are suppressed, not drawn over**, by switching each element off and
  drawing the replacement at that exact point in the render order, so chat, the tab list and
  the scoreboard still render on top.
- **Trend velocities are sampled once per tick**, detected from the GUI tick counter. The
  sampler resets its history across a gap of more than two ticks, so a paused game or an
  open screen cannot produce a phantom velocity spike.
