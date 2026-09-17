# DayZ Hotbar 1.2.0 (Minecraft 1.20.1 – 26.3)

**One source tree, 23 Minecraft versions, three loaders.** The HUD is unchanged — same
hotbar, same status readout, same icons — but the project is no longer split across
branches and per-version trees.

## What changed

- **The version-per-branch layout is gone.** The tree is now a [Stonecutter](https://stonecutter.kikugie.dev/)
  project: one `common/` source tree with the loader modules beside it, and one build node
  per (loader × Minecraft version) pair. Version differences are marked inline with
  `//? if` comments and a small set of bulk renames, instead of being maintained twice.
- **1.20.1 is back in the main tree.** It used to be the `v1.0.1` tag, so fixing anything on
  it meant replaying history; it is now `:fabric:1.20.1` like any other node.
- **Coverage runs from 1.20.1 to 26.3**, including everything in between: 1.20.2–1.20.4,
  1.20.5, 1.20.6, 1.21–1.21.11 and the unobfuscated 26.1–26.3 line. The 26.x line needs
  Java 25 and uses the rewritten GUI (`GuiGraphicsExtractor`); the HUD moved into a new
  `Hud` class there, and both are handled.
- **50 shippable jars** are produced: 23 Fabric, 9 Forge and 18 NeoForge.
- **Build with one command.** `./gradlew chiseledBuild` builds the whole matrix;
  `./gradlew matrix` lists the nodes.
- **Publishing tags every artifact correctly.** Each node uploads to Modrinth and CurseForge
  with its own game version and loader tags read from `versions/<mc>/gradle.properties`, so no
  jar can go up under the wrong Minecraft version.

## Coverage

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

The deliberate gaps — Forge 1.20.1 (SRG toolchain), and Forge 1.21 / 1.21.6 / 1.21.7 (those
Forge lines ship no HUD layer API) — are recorded with their reasons in
[docs/BUILDING.en.md](docs/BUILDING.en.md) section 7.

---

# DayZ Hotbar 1.1.0 (Minecraft 1.21.1)

**Minecraft 1.21.1, and NeoForge.** The HUD itself is unchanged — same hotbar, same
status readout, same icons. What moved is everything underneath it.

**NeoForge is new**, alongside the existing Fabric and Forge builds. All three are built
from the same source and behave identically. As before, no loader needs an API mod.

**1.20.1 is not going anywhere.** This release adds 1.21.1 rather than replacing 1.20.1:
the 1.0.1 build for 1.20.1 stays available and unchanged on both stores. Take whichever
matches the version you play — 1.20.1 for Fabric and Forge, 1.21.1 for Fabric, Forge and
NeoForge.

## What the HUD is doing on this version

1.21.1 rebuilt the vanilla HUD from the ground up, and it took both loaders with it:

- Vanilla now assembles the HUD as a **`LayeredDraw`** — a list of named layers — rather
  than calling each render method in turn.
- **Forge deleted its whole overlay system.** There is no `ForgeGui`, no
  `GuiOverlayManager`, no `VanillaGuiOverlay` and no `RenderGuiOverlayEvent` any more.
  Layers are named and reordered through `AddGuiOverlayLayersEvent` instead.
- **NeoForge never had that system** and hooks the layers at render time through
  `RenderGuiLayerEvent`.
- **Forge no longer reobfuscates.** It resolves against official Mojang names at runtime
  now, exactly like NeoForge, so the released Forge jar is the plain build output.

Each loader therefore reaches the HUD by its own route, and the Forge build no longer
carries a mixin at all — neither does NeoForge. Only the Fabric jar mixes into `Gui`.

## Requirements

| | |
| :--- | :--- |
| Minecraft | 1.21.1 |
| Java | 21 (up from 17 — 1.21.1 requires it) |
| Fabric | Loader 0.15.0+ |
| Forge | 52.1.2 or newer |
| NeoForge | 21.1.x |

**Take note of the Forge minimum.** Forge 1.21.1 shipped without any HUD API at all —
the layer hook this mod uses did not appear until 52.1.2. On 52.1.0 or 52.1.1 the mod
cannot draw anything, so it now asks for 52.1.2 and up and will say so plainly rather
than failing at load.

## One difference between the loaders

Forge keeps the slot row, the experience bar, the health row and the mount's health in a
**single** layer, so replacing it also drops two small vanilla elements that Fabric and
NeoForge keep: the brief "selected item name" popup, and the jump-charge bar you get
while riding a horse. Neither is worth reimplementing — the panel on the left of the
hotbar already shows the held item's name permanently.

## Documentation now leads in Chinese

The repository's own docs are bilingual, and the Chinese pages are the ones the links at
the top of each page point to first:

- `README.md` is now the Chinese README, and the English one moved to `README.en.md`.
  Each page links to the other at the very top, so either direction is one click.
- The Chinese page is the one GitHub shows by default, matching DayZ Inventory — the two
  mods are a pair and now read the same way in the repository.
- Code, jar names, file paths, version numbers, links and the badge row are unchanged
  between the two pages; only the prose is translated.
- Both pages have to be kept in step from here on. A feature added to one belongs in the
  other in the same change.

---

# DayZ Hotbar 1.0.1 (Minecraft 1.20.1)

**Forge support.** The mod now ships for Forge 47.x alongside Fabric, built from the same
source with the same HUD. Neither loader needs an API mod.

The first Forge build drew the hotbar but never the status readout, and left vanilla's
health and food rows on screen. Forge replaces vanilla's `Gui` with its own `ForgeGui`,
which draws each HUD element from a separate overlay and never calls into `Gui`'s
internals — so a mixin on `Gui` only ever reached the two elements Forge happens to
inherit unchanged. The Forge build now drives the HUD through Forge's own overlay events
instead, and ships no mixin at all.

Also in this release:

- The Fabric jar now contains the mod icon. The 1.0.0 jar was built before the logo
  landed, so it shipped without one.
- The release notes and both store descriptions cover Forge, and carry a GUI-scale tip.

---

# DayZ Hotbar 1.0.0 (Minecraft 1.20.1)

First release. Replaces the vanilla HUD with a DayZ-style hotbar and a DayZ-style status
readout. Fabric only.

## The hotbar

Nine slots plus the offhand, drawn on a single flat panel in the same near-black
translucent style as the DayZ Inventory screen. The grey slot is 18x18 with only a
one-pixel dark edge around it, and there are no outlines anywhere — slot state is carried
by the colour of the wash.

| State | Meaning |
| :--- | :--- |
| Dim wash | Empty |
| Lifted wash | Holds an item |
| Green wash | The slot currently in hand |
| Red wash | In hand, but the item cannot be used right now (on cooldown) |

Switching slots animates: the newly held slot starts yellow and resolves to green over
about a third of a second, so a swap reads as an event rather than an instant flip.

Vanilla draws the attack-strength indicator inside the hotbar, so replacing the hotbar
removes it along with it. It is not reimplemented — set Options → Video Settings →
Attack Indicator to *Crosshair* if you want it back.

## The player panel

The bottom-left corner carries a two-row panel on the same wash the hotbar's slots use,
with its bottom edge on the hotbar's own line so the whole HUD reads as one row.

| Row | Contents |
| :--- | :--- |
| **Held item** | A DayZ condition dot — pristine, worn, damaged, badly damaged, ruined — and the item's name |
| **Equipment** | A stance figure, a shield mark, and an armour bar |

The right half of the held-item row is left empty on purpose. That is where a weapon's
firing mode and range belong, and where an ammo mark will go once there is a gun mod to
read them from — a placeholder there would only have to be taken back out.

Condition follows DayZ's grades rather than vanilla's bar: an item with no durability at
all reports as pristine, which is truer than inventing a grade for something that cannot
wear out.

## The status readout

A horizontal row of icons in the bottom-right corner, on the same margin as the hotbar.

The row is divided into three sections, left to right:

| Section | Icons |
| :--- | :--- |
| **Effects** | One mark per family of active potion effect |
| **Sustenance** | Apple for food, with saturation drawn over it as a brighter wash; bottle for water; thermometer for temperature; bubble for air, only while you are underwater |
| **Vitals** | Drop for experience with the level number on it; gold cross for absorption; cross for health, or your mount's health while riding |

An upright line separates the sections, and only where both sides have something in them
— so the effects divider comes and goes with the effects themselves, while the one
between sustenance and vitals is always there.

Absorption is drawn as a **second cross**, badged with a small plus in its top right,
rather than as an icon of its own — the plus is what tells the two apart.

Water and temperature are drawn but not yet **read**. Water mirrors food, so it moves when
food moves and reads as a second hunger bar rather than pretending to a reading it does not
have. Temperature sits at half and white, because half is "comfortable" on a thermometer
and that is the state a player is in almost all of the time. Neither is colour-tiered while
it is standing in for a stat that does not exist yet.

### Effect marks

Active potion effects are collapsed to **one mark per family** rather than one per
effect, because Minecraft has thirty-odd effects and a row that grew per effect would
eat the screen edge. What matters at a glance is which *kinds* of thing are on you.

| Mark | Family |
| :--- | :--- |
| Heart | Beneficial — speed, strength, night vision |
| Pill | Restorative — regeneration, absorption, saturation |
| Broken heart | Harmful — poison, hunger, mining fatigue, everything else bad |

The two hearts are a pair: whole for beneficial, split down the middle for harmful. That
was deliberate over a down arrow, which was tried first and rejected — this HUD already
uses down chevrons to mean "this value is falling", so an arrow in the effects row read
as a direction rather than as a status, and said nothing about being afflicted.

They are drawn **white**, with the same outline-gap-fill treatment as every other icon,
but no colour banding and no trend marker — an effect is either on or off, so there is
nothing for a fill level to say. The pill is the one exception: it is filled to half,
because a capsule with one half filled is what makes it read as a pill rather than as a
lozenge. When an effect ends its mark **fades out** over a second rather than vanishing.

Icons that come and go do not shift the ones that are always there: the row is
right-aligned, so it grows and shrinks from the left.

### Icons are drawn as vessels

Each icon is an outline, a one-cell clear gap, and an interior that fills from the bottom
as the value rises. The outline takes the same colour as the fill, so a yellow icon has a
yellow outline.

The icons are hand-drawn pixel art on a 15x15 grid, defined in the source rather than
loaded from a texture. The mod therefore ships no icon art of its own and cannot clash
with a resource pack.

### Icons change colour as they drop

Health and food use **DayZ's own bands**, which differ from each other. Health is quoted
against 100 HP; food against a 5,000-point reserve.

| | White | Yellow | Red | Flashing |
| :--- | :--- | :--- | :--- | :--- |
| **Health** | 61–100% | 31–60% | 15–30% | 0–14% |
| **Food** | 16–100% | 6–15% | 2–5% | 0–1.9% |

Minecraft's bars are 20 points for both, so health turns yellow at 12 or less, red at 6 or
less, and flashes under 3 — while food turns yellow at 3 or less, red at 1, and flashes
only when empty. Food is deliberately the more forgiving of the two: in DayZ you are
warned about hunger far later than about blood loss.

Air has no bands of its own and borrows health's, since drowning and bleeding out are the
same kind of emergency.

Absorption and experience are deliberately **not** colour-tiered: having less absorption is
not a warning, and neither is how far through a level you are.

## Trend markers

A marker shows which way each stat is moving — a stacked chevron in the shape of a US Army
rank insignia, always white so it never competes with the tier colours. It sits **above**
the icon when the stat is rising and **below** it when falling, so the marker is on the
side the value is heading.

- **One chevron** — ordinary drift
- **Two chevrons** — a significant change, which is what makes a poison tick or a
  regeneration effect read differently from hunger ticking down on its own
- Held for a second and a half after the movement stops, then faded out — long enough
  that a short exchange of damage does not come and go before you see it

Thresholds are per stat and measured over one second. They are deliberately low for stats
that move slowly — natural health regeneration is only about 0.25 HP per second, so a
threshold of 1.0 would never fire and you would never see that you were healing.

## Notes

- **No API mod required on either loader.** No Fabric API, and nothing extra on Forge —
  each build talks to its own loader's HUD plumbing and to nothing else.
- The vanilla health, hunger, armour, air and experience elements are suppressed rather
  than drawn over, so nothing double-draws.
- Vanilla's own visibility rules are inherited: the HUD still hides behind an open screen,
  in spectator mode, and when you press F1.

## Licence

Apache License 2.0 — free to use, modify and redistribute, including commercially.
