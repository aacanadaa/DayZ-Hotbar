# DayZ Hotbar 1.0.0 (Minecraft 1.20.1)

First release. Replaces the vanilla HUD with a DayZ-style hotbar and a DayZ-style
status readout. Fabric only, for now.

## The hotbar

Nine slots plus the offhand, drawn as individual flat panels instead of one long
bar, in the same near-black translucent style as the DayZ Inventory screen.

Each slot's outline carries its state:

| State | Meaning |
| :--- | :--- |
| Grey | Empty |
| Light grey | Holds an item |
| Green | The slot currently in hand |
| Red | In hand, but the item cannot be used right now (on cooldown) |

The held slot also gets a coloured wash behind the item, so it reads at a glance
without having to compare outline colours.

Durability is deliberately **not** part of this yet — that is a separate feature
for later.

Vanilla draws the attack-strength indicator inside the hotbar, so replacing the
hotbar would have silently removed it. A compact replacement is drawn to the
right of the offhand slot.

## The status readout

A horizontal row of icons in the bottom-right corner, each filled to its current
level rather than showing a number.

| Icon | Notes |
| :--- | :--- |
| Health | Shows your mount's health while riding, as vanilla does |
| Food | Saturation is drawn over the food level as a brighter wash |
| Armor | Only appears when you are wearing some |
| Air | Only appears while you are underwater |
| Absorption | Only appears while you have golden hearts |
| Experience | A bar with the level number above it |

Icons that come and go do not shift the ones that are always there: the row is
right-aligned, so it grows and shrinks from the left.

### Trend arrows

Under each icon, a small chevron shows which way the stat is moving:

- **One chevron** — ordinary drift
- **Two chevrons** — a significant change, which is what makes a poison tick or a
  regeneration effect read differently from hunger ticking down on its own
- **Green** rising, **red** falling
- The arrow fades out a moment after the movement stops, rather than flickering

Thresholds are per stat and measured over one second. They are deliberately low
for stats that move slowly — natural health regeneration is only about 0.25 HP
per second, so a threshold of 1.0 would never fire and you would never see that
you were healing.

## Reusing Minecraft's own icons

The status icons are Minecraft's own sprites, read from the vanilla
`textures/gui/icons.png` sheet and cropped to a fraction of their height. That
means the mod ships no icon art of its own, cannot clash with a resource pack,
and still reads as Minecraft rather than as a texture pack someone bolted on.

## Notes

- **No Fabric API required.** The HUD is installed with Mixin against vanilla's
  own `Gui`, so Fabric Loader is the only dependency.
- The vanilla health, hunger, armour, air and experience elements are suppressed
  rather than drawn over, so nothing double-draws.
- Vanilla's own visibility rules are inherited: the HUD still hides behind an
  open screen, in spectator mode, and when you press F1.
