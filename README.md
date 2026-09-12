# DayZ Hotbar

[![License](https://img.shields.io/badge/License-PolyForm_Noncommercial-blue.svg)](LICENSE)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62b47a.svg)
![Loader](https://img.shields.io/badge/Loader-Fabric-dbb69b.svg)
![Java](https://img.shields.io/badge/Java-17-e76f00.svg)
![Environment](https://img.shields.io/badge/Environment-Client-9cf.svg)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Hotbar?color=red)](https://github.com/aacanadaa/DayZ-Hotbar/issues)
[![Last commit](https://img.shields.io/github/last-commit/aacanadaa/DayZ-Hotbar)](https://github.com/aacanadaa/DayZ-Hotbar/commits/main)

Replaces the Minecraft HUD with a DayZ-style hotbar and a DayZ-style status readout,
styled to match [DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory).

**Available for Minecraft 1.20.1 on Fabric.**

---

## Features

### DayZ-style hotbar

Nine slots plus the offhand on a single flat panel, in the same near-black translucent
style as the DayZ Inventory screen. The grey slot is 18x18 with only a one-pixel dark
edge — state is carried by the colour of that wash, never by a border.

Switching slots animates: the newly held slot starts yellow and resolves to green, so a
swap reads as an event rather than an instant flip.

### Status readout

A horizontal row of icons in the bottom-right, each filled to its current level rather
than showing a number: food with saturation, armor, air, absorption, experience with its
level, and health at the rightmost end.

Each icon is drawn as a **vessel** — an outline, a clear gap, and an interior that fills
from the bottom as the value rises. It changes colour as it drops:

| Band | Colour |
| :--- | :--- |
| Above half | White |
| Below half | Yellow |
| Below a fifth | Red |
| Below about a thirteenth | Red, flashing |

### Trend markers

A stacked chevron above or below each icon shows which way the stat is moving — above
when rising, below when falling. One chevron for ordinary drift, two for a significant
change, which is what makes a poison tick read differently from hunger ticking down on
its own.

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
- **Changelog**: [CHANGELOG.md](CHANGELOG.md)
- **DayZ Inventory**: <https://github.com/aacanadaa/DayZ-Inventory>

---

## License & Copyright

Licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE).

This is a **source-available** licence, not an open source one. You are free to use,
modify and redistribute the mod — including in modpacks and on servers — but **not for
commercial purposes**. The licence spells out exactly what that covers.

Copyright 2026 suoim.
