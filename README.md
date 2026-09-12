# DayZ Hotbar

Replaces the Minecraft HUD with a DayZ-style hotbar and a DayZ-style status
readout, styled to match [DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory).

**Available for Minecraft 1.20.1 on Fabric.**

---

## Features

### DayZ-style hotbar

Nine slots plus the offhand, drawn as individual flat panels in the same
near-black translucent style as the DayZ Inventory screen. Slot outlines carry
their state — grey for empty, light grey for holding an item, green for the slot
in hand, and red for a held item that cannot be used right now.

### Status readout

A horizontal row of icons in the bottom-right corner, each filled to its current
level rather than showing a number: health, food with saturation, armor,
absorption, air, and experience with its level number. Icons that only sometimes
apply — armor you are not wearing, air while you are on land — appear and
disappear without shifting the rest of the row.

### Trend arrows

Under each icon, a chevron shows which way the stat is moving. One chevron for
ordinary drift, two for a significant change, green rising and red falling. The
arrow fades shortly after the movement stops.

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1.
2. Drop the jar into your `mods` folder.
3. That's it — Fabric API is **not** required.

---

## Dependencies

| | |
| :--- | :--- |
| Minecraft | 1.20.1 |
| Fabric Loader | 0.15.0 or newer |
| Java | 17 or newer |
| Fabric API | Not required |

---

## Building from Source

Requires **JDK 17**.

```bash
./gradlew build
./gradlew :fabric:build
```

The mod jar lands in `fabric/build/libs/`.

---

## Links

- **Source**: <https://github.com/aacanadaa/DayZ-Hotbar>
- **Issues**: <https://github.com/aacanadaa/DayZ-Hotbar/issues>
- **DayZ Inventory**: <https://github.com/aacanadaa/DayZ-Inventory>

---

## License & Copyright

Licensed under the [Apache License 2.0](LICENSE).

Copyright 2026 suoim.
