# Building DayZ Hotbar

[中文](BUILDING.md) | **English**

This document explains how the unified multi-version / multi-loader build is put together,
why it is shaped the way it is, and how to add another Minecraft version.

---

## 1. Why one tree instead of one branch per version

The project used to carry a branch per Minecraft version. Every bug fix had to be merged
into each of them, and each merge had to be built and tested separately. 1.20.1 in
particular was the `v1.0.1` tag rather than a live branch, so fixing anything on it meant
replaying history.

It is now one branch with one source tree. Version-specific code is marked inline with
`//? if <condition>` comments, and the build produces one artifact per *node* — a node
being a (module, Minecraft version) pair such as `:fabric:26.2`. The tree covers **23
Minecraft versions** (1.20.1 through 26.3) and produces **50 shippable jars**.

[Stonecutter's own guide](https://stonecutter.kikugie.dev/) describes both a **flat** layout
(one `src/`, one node per version *and* loader, loader logic selected by build constants)
and a **branched** layout (a shared `common/` plus loader modules, each with its own set of
version nodes). This project uses the branched layout, because the two loaders genuinely
need different toolkits — Fabric Loom and NeoForge ModDevGradle — and because it keeps the
existing `common/` / `fabric/` / `forge/` / `neoforge/` separation that the codebase already
had.

[Architectury](https://docs.architectury.dev/) was evaluated as the loader-abstraction layer
and is **not** used. Two reasons:

1. Architectury API would become a hard runtime dependency of every download. This mod
   already reaches the HUD through each loader's *own* native mechanism (a Mixin on Fabric,
   a layer renderer on Forge and NeoForge), so an abstraction layer would add a dependency
   without removing the per-loader code that actually has to exist.
2. From 26.1 Minecraft ships **unobfuscated** and Fabric Loom split into `fabric-loom`
   (26.1+) and `fabric-loom-remap` (<26.1). Architectury Loom adds a second layer on top of
   that split with no established precedent on 26.x, which is the version that matters most.

---

## 2. Directory layout

```
settings.gradle.kts          Stonecutter tree + plugin management + the version matrix
stonecutter.gradle.kts       Controller script: active version, chiseledBuild, publishAll, matrix
gradle.properties            Mod metadata, publishing ids, shared tool versions
build-logic/                 Convention plugins shared by every node
versions/<mc>/gradle.properties   Per-version dependency coordinates

common/                      Loader-agnostic HUD code + the Fabric mixin
  build.gradle.kts           Runs once per Minecraft version
  src/main/java              Shared sources, with `//? if` markers
fabric/                      Fabric entrypoint (the mixin lives in common)
neoforge/                    NeoForge entrypoint + NeoForgeHudHandler
forge/                       Forge entrypoint + ForgeHudHandler
```

`stonecutter` names each node after its Minecraft version and places it under the branch
directory, so `:fabric:26.2` has its project directory at `fabric/versions/26.2/`. Those
directories are build output; only `versions/<mc>/gradle.properties` is source-controlled.

---

## 3. How the Gradle build fits together

### `settings.gradle.kts`

Declares the tree and the matrix. Two details are load-bearing and easy to break:

- **The root branch is deliberately empty.** It exists (Stonecutter always has one) but
  lists no versions. Giving it versions would create a project per Minecraft version with no
  build script and no artifacts, and the controller's publishing tasks cannot be ordered
  across projects that do not have them.
- **`pluginManagement.plugins {}` names both Loom flavours.** `dev.kikugie.loom-back-compat`
  applies one of `net.fabricmc.fabric-loom` / `net.fabricmc.fabric-loom-remap`
  *programmatically*, and a programmatic `pluginManager.apply(id)` is resolved against the
  project's buildscript repositories rather than against `pluginManagement.repositories`.
  Naming both ids there — and declaring them with `apply false` in `stonecutter.gradle.kts`
  — is what makes them resolvable from every node.

### `build-logic/`

Two precompiled convention plugins:

| Plugin | Applied to | Responsibility |
| :--- | :--- | :--- |
| `dayz-common` | every node | version strings, Java toolchain, repositories, manifest expansion, version renames, licence in the jar, generated-source wiring |
| `dayz-loader` | fabric / neoforge / forge | sharing the `common` node's sources, the packaging check, and `publishMods` |

`build-logic/src/main/kotlin/Utils.kt` holds the `prop()` / `mc` / `branch` accessors. Note
that `stonecutter { }` is *not* available inside a precompiled script plugin — the extension
has to be reached through the `sc` accessor.

### Per-version properties

`versions/<mc>/gradle.properties` is the single place a Minecraft version's coordinates are
declared (loader versions, Java level, mixin compatibility level, pack format, publishing
range). Stonecutter only reads those files for versions registered on the *root* branch, and
the root branch is empty here, so `Utils.kt` reads them directly.

---

## 4. Version-processed sources

Stonecutter preprocesses the shared `src/` trees and writes the result to

```
<branch>/versions/<mc>/build/generated/stonecutter/<sourceSet>/{java,resources}
```

`dayz-common` points `compileJava` at that generated tree and **not** at the raw sources,
because the raw tree contains every version's code side by side and only comments out the
inactive branches in the copy it generates.

`dayz-loader` then adds the *common* node's generated tree to the loader's own compile task,
so each loader jar contains one copy of every shared class. For Fabric, the mixin refmap can
only be produced by running the Mixin annotation processor over the annotated *sources*, which
is why the shared sources (not a precompiled `common.jar`) are compiled into the loader.

The shared `GuiMixin` is Fabric's mechanism, so NeoForge and Forge **exclude it** from their
compile and their jar: those loaders reach the HUD through their own layer renderer, and
loading the mixin there would draw the HUD twice.

---

## 5. Conditional compilation

Two mechanisms are used.

### `//? if` comments

For anything structural — a different method signature, a different hook target, a method
that was removed:

```java
//? if >=1.21 {
private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
//?} else {
private static void render(GuiGraphicsExtractor graphics, float partialTick) {
//?}
```

**Do not put comments inside a conditional block** — the processor rewrites comment markers
and a surviving `//` can end up stripped, which turns a comment into code.

### `replacements`

For pure renames that would otherwise put a `//? if` around the same identifier dozens of
times, `dayz-common` declares bulk rewrites:

```kotlin
sc.replacements.string(sc.current.parsed < "1.21.5") {
    replace("getInventory().getSelectedSlot()", "getInventory().selected")
}
```

The sources are written against the **newest** names (`getSelectedSlot()`,
`GuiGraphicsExtractor`, `MobEffects.INSTANT_HEALTH`), and the renames are applied
*backwards* for older nodes.

**Stonecutter runs a replacement in both directions** — forwards when the condition holds,
backwards when it does not — so the longer spelling must not be a superstring of anything on
the other side. `MobEffects.HEAL` is a prefix of `HEALTH_BOOST`, and a naive
`replace("MobEffects.INSTANT_HEALTH", "MobEffects.HEAL")` turned the reverse pass into
`INSTANT_HEALTHTH_BOOST`. Anchoring each spelling to the `.value()` call or a trailing comma
keeps them unambiguous.

---

## 6. The version matrix

| Minecraft | Fabric | Forge | NeoForge | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1** | ✅ | — | — | 17 |
| **1.20.2** | ✅ | — | — | 17 |
| **1.20.3** | ✅ | — | — | 17 |
| **1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6** | ✅ | ✅ | ✅ | 21 |
| **1.21** | ✅ | — | ✅ | 21 |
| **1.21.1** | ✅ | ✅ | ✅ | 21 |
| **1.21.2** | ✅ | — | ✅ | 21 |
| **1.21.3** | ✅ | ✅ | ✅ | 21 |
| **1.21.4** | ✅ | ✅ | ✅ | 21 |
| **1.21.5** | ✅ | ✅ | ✅ | 21 |
| **1.21.6** | ✅ | — | ✅ | 21 |
| **1.21.7** | ✅ | — | ✅ | 21 |
| **1.21.8** | ✅ | ✅ | ✅ | 21 |
| **1.21.9** | ✅ | ✅ | ✅ | 21 |
| **1.21.10** | ✅ | ✅ | ✅ | 21 |
| **1.21.11** | ✅ | ✅ | ✅ | 21 |
| **26.1** | ✅ | — | ✅ | 25 |
| **26.1.1** | ✅ | — | ✅ | 25 |
| **26.1.2** | ✅ | — | ✅ | 25 |
| **26.2** | ✅ | — | ✅ | 25 |
| **26.3** | ✅ | — | ✅ | 25 |

Totals: **23 Minecraft versions, 50 artifacts** (23 Fabric, 9 Forge, 18 NeoForge).
`./gradlew matrix` prints the authoritative node list.

### Per-version HUD API boundaries

Established by inspecting each version's real jar, not inferred from the version number.

| Since | Change |
| :--- | :--- |
| 1.20.2 | Fabric only (networking changed, but this mod has no packets). `ItemCooldowns#isOnCooldown` still takes an `Item` |
| 1.20.5 | Effects become `Holder<MobEffect>`; `MobEffectInstance#getEffect()` returns a holder. Vanilla adopts `LayeredDraw` and the hotbar becomes `renderItemHotbar`; `renderExperienceLevel` is split out. `Gui#render` still takes a `float` |
| 1.21 | `Gui#render` and `renderItemHotbar` take a `DeltaTracker` |
| 1.21.2 | `ItemCooldowns#isOnCooldown` takes an `ItemStack`. Food, armour and air are drawn from `Gui#render` rather than from `renderPlayerHealth`, so they need their own cancellations |
| 1.21.5 | `Inventory#selected` becomes private, replaced by `getSelectedSlot()`. `MobEffects.HEAL` is renamed `INSTANT_HEALTH` |
| 1.21.6 | The GUI stack swaps to `Matrix3x2f` (`pushPose`→`pushMatrix`, 3-arg `translate`/`scale` lose z). The experience bar becomes a "contextual bar" and the whole bottom block is drawn by `renderHotbarAndDecorations` |
| 1.21.9 | `Level#isClientSide` becomes private (unused here) |
| 1.21.11 | `ResourceLocation` is renamed `Identifier` |
| 26.1 | Minecraft ships unobfuscated: no mappings, no refmaps, no remap step. `GuiGraphics` → `GuiGraphicsExtractor`, every `render*` → `extract*`, `drawString`→`text`, `renderItem`→`item`, `renderItemDecorations`→`itemDecorations` |
| 26.2 | The HUD moves out of `Gui` into a new `Hud` class; `getGuiTicks()` and the hidden flag move with it, and `Options.hideGui` becomes `Hud.isHidden()`. Forge/NeoForge layer ids for the status row are already per-element |
| 26.3 | `InputConstants.Type.KEYSYM` → `KEYBOARD` (unused here) |

---

## 7. Enabled targets and the gaps that remain

### Forge 1.20.1

Not built. Forge 1.20.1 runs on SRG names, so it needs reobfuscation plus a Searge mixin
refmap. ForgeGradle 7 has neither, and ForgeGradle 6 — which has both — is Gradle 8 only
while Loom 1.18.1 needs Gradle 9. 1.20.1 still ships for Fabric.

### Forge 1.21, 1.21.6, 1.21.7

Not built. Inspecting the Forge universal jars for 51.0.33, 56.0.9 and 57.0.3 shows **no**
`ForgeLayeredDraw` and no `AddGuiOverlayLayersEvent` — Forge was mid-rewrite and shipped no
HUD hook at all on those lines, so there is nothing for this mod to hook. NeoForge covers
1.21.6 and 1.21.7, and Fabric covers 1.21.

### Forge 1.21.2

Not built: Forge never published that release.

### Forge 26.x

Not built: Forge has no buildable 26.x line here; NeoForge is the supported route.

### NeoForge below 1.20.6

Not built. NeoForge forked from Forge at 1.20.2, and its 20.2.x–20.5.x lines predate the
`RenderGuiLayerEvent` layer hook this mod is built on. 1.20.1 predates NeoForge entirely.
Fabric covers every version from 1.20.1.

Fabric and NeoForge together cover every version from 1.20.6 upward, plus Fabric alone for
1.20.1–1.20.5.

---

## 8. Adding a Minecraft version

1. Add `versions/<mc>/gradle.properties`, copying the closest existing version and updating
   `deps.minecraft`, `deps.java`, `deps.mixin-compat`, `deps.pack-format`, the loader versions
   and `meta.minecraft-range`.
2. Add the version to the relevant list in `settings.gradle.kts`.
3. Run `./gradlew :common:<mc>:compileJava` and port what breaks. Add it to `forgeVersions`
   as well only if the Forge line ships the layer API (see section 7).
4. Add the version to `README.md` / `README.en.md` and to the store descriptions in
   `docs/store-descriptions/`.
5. `./gradlew chiseledBuild` to confirm the whole matrix still builds.

Publishing picks the new version up automatically: the game version tag and the loader tag
are read from the node, not hardcoded per release.

---

## 9. Publishing

`dayz-loader` configures `me.modmuss50.mod-publish-plugin` for every loader node:

- Modrinth project `hsTmy4Hi`, CurseForge project `1693963`.
- Tokens come from the environment: `MODRINTH_TOKEN` (falls back to `MODRINTH_PAT`) and
  `CURSEFORGE_API_KEY` (falls back to `CURSEFORGE_TOKEN`).
- `publish.dry_run` defaults to `true`, so a stray `publishMods` cannot submit anything.
- `publishAll` depends on every node's `publishMods`, ordered so CurseForge receives one
  upload at a time instead of a burst.

The mod declares **no dependencies** on either store — it does not use Fabric API and needs
no loader API mod — so nothing extra is offered to users.

**CurseForge never replies with a URL.** Every upload goes into human review; the API accepts
the file and returns. A green `publishCurseforge` therefore means *submitted*. Modrinth
returns immediately.

The CurseForge project **description** cannot be updated from the build — the upload token
is a legacy upload-only token and the Eternal API that edits metadata rejects it.
`docs/store-descriptions/curseforge.md` is the copy-paste source for that page and has to be
kept in sync by hand. The Modrinth description *is* pushed via the API.

---

## 10. Gotchas

1. **`gradlew` must stay executable** (mode `100755`). CI also runs `chmod +x ./gradlew`.
2. **The Gradle wrapper is 9.7.0.** Fabric Loom 1.18.1 publishes
   `org.gradle.plugin.api-version = 9.7.0`; an older wrapper fails to resolve it with a
   variant-matching error that does not mention the Gradle version.
3. **Do not hardcode `org.gradle.java.home`.** It is machine-specific and breaks CI. The
   launcher JDK is supplied through `JAVA_HOME`; per-version toolchains come from
   `versions/<mc>/gradle.properties` and are downloaded by the foojay resolver.
4. **`prop()` reads two sources.** `findProperty` first (so `-P` and the root
   `gradle.properties` win), then `versions/<mc>/gradle.properties`.
5. **Never put comments inside a `//? if` block.**
6. **A mixin whose target has been renamed does not degrade, it crashes.** The client mixin
   config is `required: true`. When a target moves, grep for it and hook the method
   everything routes through; do not guess.
7. **Replacements are bidirectional.** Anchoring the two spellings so neither is a substring
   of the other is what keeps the reverse pass from corrupting a newer version.
8. **The Forge layer tree changed shape.** 1.20.6–1.21.5 use `HOTBAR` + `EXPERIENCE` under
   `PRE_SLEEP_STACK`; 1.21.8–1.21.10 use a single `HOTBAR_AND_DECOS`; 1.21.11 uses
   `ITEM_HOTBAR` + `HEALTH_BAR` + `VEHICLE_HEALTH` + `EXPERIENCE_LEVEL` + `CONTEXTUAL_INFO`.
9. **1.21.6 moved Forge to EventBus 7**, which split `net.minecraftforge.eventbus.api` into
   `bus` and `listener`; `@SubscribeEvent` comes from
   `net.minecraftforge.eventbus.api.listener` from that version on.
