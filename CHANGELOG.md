# DayZ Hotbar 1.0.0 (Minecraft 1.20.1)

First release. Replaces the vanilla HUD with a DayZ-style hotbar and a DayZ-style status
readout. Shipped for **Fabric and Forge** — the same mod built twice, not two mods.

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

- **No Fabric API required.** The HUD is installed with Mixin against vanilla's own `Gui`,
  so Fabric Loader is the only dependency.
- The vanilla health, hunger, armour, air and experience elements are suppressed rather
  than drawn over, so nothing double-draws.
- Vanilla's own visibility rules are inherited: the HUD still hides behind an open screen,
  in spectator mode, and when you press F1.

## Licence

Apache License 2.0 — free to use, modify and redistribute, including commercially.
