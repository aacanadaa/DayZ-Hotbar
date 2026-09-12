# DayZ Hotbar 1.0.0 (Minecraft 1.20.1)

First release. Replaces the vanilla HUD with a DayZ-style hotbar and a DayZ-style
status readout. Fabric only, for now.

## The hotbar

Nine slots plus the offhand, drawn on a single flat panel in the same near-black
translucent style as the DayZ Inventory screen. The slot geometry is that mod's own:
an 18px pitch with a 16x16 inner wash and **no outline** — state is carried by the
colour of the wash, never by a border.

| State | Meaning |
| :--- | :--- |
| Dim wash | Empty |
| Lifted wash | Holds an item |
| Green wash + underline | The slot currently in hand |
| Red wash + underline | In hand, but the item cannot be used right now (on cooldown) |

Switching slots animates: the newly held slot starts **yellow** and resolves to green
over about a third of a second, so a swap reads as an event rather than an instant
flip.

Durability is deliberately **not** part of this yet — that is a separate feature for
later.

Vanilla draws the attack-strength indicator inside the hotbar, so replacing the hotbar
would have silently removed it. A compact replacement is drawn to the right of the
offhand slot.

## The status readout

A horizontal row of icons on a shared panel in the bottom-right corner, each filled to
its current level rather than showing a number.

| Icon | Notes |
| :--- | :--- |
| Apple | Food level, with saturation drawn over it as a brighter wash |
| Chestplate | Armor. Only appears when you are wearing some |
| Bubble | Air. Only appears while you are underwater |
| Gold heart | Absorption. Only appears while you have golden hearts |
| Bar + number | Experience, with the level above it |
| Cross | Health, or your mount's health while riding. Rightmost, as in DayZ |

Icons that come and go do not shift the ones that are always there: the row is
right-aligned, so it grows and shrinks from the left.

### Icons change colour as they drop

Each icon is coloured by how much is left, so a glance is enough:

| Band | Colour |
| :--- | :--- |
| Above half | White |
| Below half | Yellow |
| Below a fifth | Red |
| Below about a thirteenth | Red, flashing |

On a 20-point scale that puts yellow under 10, red under 4, and the flash under 1.5 —
so food turns yellow at half a bar, red under two shanks, and flashes on the last one.

### Trend chevrons

Under each icon, a marker shows which way the stat is moving — drawn as a bold stacked
chevron in the shape of a US Army rank insignia rather than a thin arrow, and always
white so it never competes with the tier colours above it.

- **One chevron** — ordinary drift
- **Two chevrons** — a significant change, which is what makes a poison tick or a
  regeneration effect read differently from hunger ticking down on its own
- Pointing up when rising, down when falling
- Held for a second and a half after the movement stops, then faded out — long
  enough that a short exchange of damage does not come and go before you see it

Thresholds are per stat and measured over one second. They are deliberately low for
stats that move slowly — natural health regeneration is only about 0.25 HP per second,
so a threshold of 1.0 would never fire and you would never see that you were healing.

## Hand-drawn icons

The icons are pixel art defined in the source as 9x9 character grids, drawn as
rectangles at 2px per cell and cropped to the fill level. Nothing is loaded from a
texture, so the mod ships no icon art of its own and cannot clash with a resource pack.

Drawing them rather than blitting them is also what makes the recolouring possible: a
bitmap would need redrawing for every tier, while a shape just gets drawn twice.

## Notes

- **No Fabric API required.** The HUD is installed with Mixin against vanilla's own
  `Gui`, so Fabric Loader is the only dependency.
- The vanilla health, hunger, armour, air and experience elements are suppressed
  rather than drawn over, so nothing double-draws.
- Vanilla's own visibility rules are inherited: the HUD still hides behind an open
  screen, in spectator mode, and when you press F1.
