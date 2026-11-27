# DayZ Hotbar

[English](README.md) Â· [ä¸­æ–‡](README.zh.md) Â· **FranÃ§ais** Â· [æ—¥æœ¬èªž](README.ja.md) Â· [í•œêµ­ì–´](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-hotbar?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-hotbar)
[![CurseForge](https://img.shields.io/curseforge/v/1693963?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%E2%80%93%2026.3-62b47a.svg)](https://modrinth.com/mod/dayz-hotbar/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-hotbar?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-hotbar)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1693963?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Hotbar?color=red)](https://github.com/aacanadaa/DayZ-Hotbar/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Remplace le HUD de Minecraft par une barre d'accÃ¨s rapide, un panneau joueur et une ligne de statuts faÃ§on DayZ, assortis Ã  [DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory).

Mod purement client. Aucune API requise sur Fabric, Forge ou NeoForge.

![HUD DayZ Hotbar en jeu](docs/screenshots/uwu.png)
![DayZ Hotbar avec DayZ Inventory](https://cdn.modrinth.com/data/8asZxzdc/images/66b7282b83c958bd63ec912c7353bb4817bc202a.png)

---

## Versions supportÃ©es

| Minecraft | Fabric | Forge | NeoForge | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1 â€“ 1.20.4** | âœ… | â€” | â€” | 17 |
| **1.20.5** | âœ… | â€” | â€” | 21 |
| **1.20.6 â€“ 1.21.1** | âœ… | âœ… | âœ… | 21 |
| **1.21.2** | âœ… | â€” | âœ… | 21 |
| **1.21.3 â€“ 1.21.5** | âœ… | âœ… | âœ… | 21 |
| **1.21.6 â€“ 1.21.7** | âœ… | â€” | âœ… | 21 |
| **1.21.8 â€“ 1.21.11** | âœ… | âœ… | âœ… | 21 |
| **26.1 â€“ 26.3** | âœ… | â€” | âœ… | 25 |

Prenez le fichier correspondant Ã  votre version de Minecraft et Ã  votre chargeur de mods (par exemple `dayz-hotbar-fabric-1.21.1-2.0.0+mc1.21.1.jar`).

---

## FonctionnalitÃ©s

### Barre d'accÃ¨s rapide et barre de saut
- 9 emplacements + main secondaire sur une rangÃ©e translucide.
- Case vide discrÃ¨te, case occupÃ©e plus claire, transition jaune vers vert lors du changement d'objet, rouge si l'objet est en recharge.
- Ã€ cheval (ou autre monture sauteuse), une fine barre de saut apparaÃ®t au-dessus de la barre sans la chevaucher.

![Barre d'accÃ¨s rapide avec objets](docs/screenshots/hud-full-hotbar.png)

### Statuts
- En bas Ã  droite, icÃ´nes pixel-art sans texture (aucun conflit avec les packs de ressources) :
  - **Effets :** CÅ“ur (positif), gÃ©lule (rÃ©gÃ©nÃ©ration), cÅ“ur brisÃ© (nÃ©gatif), avec fondu Ã  la fin de l'effet.
  - **Survie :** Faim (avec saturation), soif, tempÃ©rature, air (sous l'eau uniquement).
  - **Vie & XP :** Goutte d'XP avec niveau, croix d'absorption avec `+`, vie (ou vie de la monture).
- **Tough As Nails & Legendary Survival Overhaul :** Lit automatiquement la soif et la tempÃ©rature corporelle lorsqu'un des deux mods est installÃ© (couleurs froid/chaud et clignotement aux extrÃªmes). Sans eux, l'eau suit la faim et la tempÃ©rature reste neutre.
- **Seuils et chevrons :** La vie, la faim, l'eau et l'air passent au jaune, au rouge puis clignotent. Les chevrons au-dessus ou en dessous indiquent une hausse ou une baisse (1 chevron pour une dÃ©rive normale, 2 pour un changement rapide).

![IcÃ´nes critiques clignotant en rouge](docs/screenshots/uwu-icons.gif)
![Statuts avec effets de potion actifs](docs/screenshots/hud-effect-marks.png)

### Panneau joueur
- En bas Ã  gauche : nom et point d'Ã©tat DayZ de l'objet tenu (neuf, usÃ©, endommagÃ©, trÃ¨s endommagÃ©, dÃ©truit), posture (marche, sprint, accroupi) et barre d'armure.
- Se place automatiquement au-dessus de la barre sur les fenÃªtres Ã©troites ou en **taille d'interface 4** pour Ã©viter tout chevauchement.

![Panneau joueur avec Ã©tat de l'outil et armure](docs/screenshots/hud-held-tool.png)

---

## Configuration

`config/dayz_hotbar.properties` est crÃ©Ã© au premier lancement :

| Option | DÃ©faut | Description |
| :--- | :--- | :--- |
| `enableHotbar` | `true` | Remplacer la barre d'accÃ¨s rapide vanilla |
| `enablePlayerPanel` | `true` | Afficher le panneau objet/armure en bas Ã  gauche |
| `enableStatusRow` | `true` | Afficher les icÃ´nes de statut en bas Ã  droite |
| `showJumpBar` | `true` | Afficher la barre de saut au-dessus de la barre |
| `showEffects` | `true` | Afficher les icÃ´nes d'effets de potion |
| `showHealth` | `true` | Afficher la vie |
| `showFood` | `true` | Afficher la faim |
| `showWater` | `true` | Afficher la soif |
| `showTemperature` | `true` | Afficher la tempÃ©rature |
| `showAir` | `true` | Afficher l'air sous l'eau |
| `showAbsorption` | `true` | Afficher l'absorption |
| `showXp` | `true` | Afficher l'XP |
| `toughAsNailsIntegration` | `true` | Lire soif et tempÃ©rature depuis Tough As Nails |
| `legendarySurvivalIntegration` | `true` | Lire soif et tempÃ©rature depuis Legendary Survival Overhaul |

---

## Notes
- Les rÃ¨gles d'affichage vanilla restent actives (F1, mode spectateur, menus ouverts).
- L'indicateur d'attaque de la barre disparaÃ®t avec elle ; passez **Options graphiques â†’ Indicateur d'attaque** sur *Viseur* pour le conserver.

---

## Compiler depuis les sources

Utilise [Stonecutter](https://stonecutter.kikugie.dev/) avec **JDK 25** pour Gradle :

```bash
./gradlew chiseledBuild          # Compiler les 50 jars
./gradlew :fabric:1.21.1:build   # Compiler une cible
./gradlew matrix                 # Lister les nÅ“uds
```
