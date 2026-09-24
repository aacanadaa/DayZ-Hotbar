# DayZ Hotbar

[English](README.md) · [中文](README.zh.md) · **Français** · [日本語](README.ja.md) · [한국어](README.ko.md)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-hotbar?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-hotbar)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1693963?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%20...%20%7C%2026.3-62b47a.svg)
![Loader](https://img.shields.io/badge/Loader-Fabric%20%7C%20Forge%20%7C%20NeoForge-dbb69b.svg)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Hotbar?color=red)](https://github.com/aacanadaa/DayZ-Hotbar/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Remplace le HUD de Minecraft par une barre d'accès rapide (hotbar) au style DayZ et une
lecture de statut au style DayZ, stylisées pour correspondre à
[DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory).

**Disponible pour Minecraft 1.20.1 jusqu'à 26.3, depuis une seule source et trois
chargeurs. Aucun chargeur n'a besoin d'un mod d'API.**

## Versions prises en charge

La matrice complète des versions est construite depuis une seule source avec la
compilation conditionnelle [Stonecutter](https://stonecutter.kikugie.dev/), produisant un
jar par paire (chargeur × version du jeu).

| Minecraft | Fabric | Forge | NeoForge | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1 – 1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6 – 1.21.1** | ✅ | ✅ | ✅ | 21 |
| **1.21.2** | ✅ | — | ✅ | 21 |
| **1.21.3 – 1.21.5** | ✅ | ✅ | ✅ | 21 |
| **1.21.6 – 1.21.7** | ✅ | — | ✅ | 21 |
| **1.21.8 – 1.21.11** | ✅ | ✅ | ✅ | 21 |
| **26.1 – 26.3** | ✅ | — | ✅ | 25 |

Forge n'a pas de 1.21, 1.21.6 ni 1.21.7 : ces lignes Forge ne publient pas d'API de
couche HUD à laquelle se brancher. Forge n'a pas non plus de 1.21.2 (jamais publiée) ni
de 26.x. Le raisonnement complet est consigné dans
[docs/BUILDING.en.md](docs/BUILDING.en.md).

Chaque build dessine le même HUD, mais les jars ne sont **pas** interchangeables — prenez
celui qui correspond à votre version du jeu et à votre chargeur.

> **Conseil — Échelle de l'interface.** Le HUD est disposé à une taille en pixels fixe et
> réglé pour l'échelle d'*Automatique* par défaut de Minecraft. À une échelle **grande**,
> la hotbar, le panneau du joueur et la lecture de statut se compriment et peuvent se
> rencontrer au milieu de l'écran ; à une échelle très **petite**, les icônes deviennent
> difficiles à lire. Si l'un ou l'autre se produit, ajustez **Options → Paramètres vidéo →
> Échelle de l'interface**.

![Le HUD DayZ Hotbar en jeu : le panneau de l'objet tenu en bas à gauche montrant une main vide, une hotbar de neuf emplacements avec l'emplacement tenu éclairé en vert, et la lecture de statut en bas à droite montrant la nourriture, l'eau, la température, l'expérience et la santé](docs/screenshots/uwu.png)
![Interface de DayZ Inventory — grille Vicinity avec un tiroir Jukebox ouvert, le panneau Survivor, l'emplacement Hands 2.0x montrant une Decorated Pot, et la grille de fabrication 2x2](https://cdn.modrinth.com/data/8asZxzdc/images/66b7282b83c958bd63ec912c7353bb4817bc202a.png)

Avec le mod d'inventaire ^^

---

## Fonctionnalités

### La hotbar

Neuf emplacements plus l'offhand sur une seule ligne plate, dans le même style translucide
quasi noir que l'écran DayZ Inventory. Chaque boîte grise fait 22x22 avec un pixel de
dégagement entre les boîtes, et il n'y a ni plaque de fond ni contours nulle part — l'état
d'un emplacement est porté uniquement par la couleur du fond teinté.

| État | Signification |
| :--- | :--- |
| Fond atténué | Vide |
| Fond éclairci | Contient un objet |
| Fond vert | L'emplacement actuellement en main |
| Fond rouge | En main, mais l'objet est en recharge et ne peut pas être utilisé |

Changer d'emplacement est animé : le nouvel emplacement tenu commence en **jaune** et se
résout en **vert** en environ un tiers de seconde, si bien qu'un changement se lit comme
un événement plutôt que comme un basculement instantané.

![La hotbar portant une épée, une pioche, un tas de steaks, une torche et un tas de pommes dorées, avec l'emplacement du steak tenu éclairé en vert et les quantités d'objets dessinées sur les emplacements empilés](docs/screenshots/hud-full-hotbar.png)

### La lecture de statut

Une ligne horizontale d'icônes en bas à droite, sur la même marge que la hotbar, de sorte
que tout le HUD se lit comme une ligne à travers l'écran. Chaque icône est dessinée comme
un **vaisseau** — un contour, une case de dégagement vide, et un intérieur qui se remplit
par le bas à mesure que la valeur monte. Le contour prend la même couleur que le
remplissage ; une icône jaune a donc un contour jaune.

La ligne est divisée en trois sections, de gauche à droite :

| Section | Icônes |
| :--- | :--- |
| **Effets** | Une marque par famille d'effet de potion actif |
| **Sustentation** | Pomme pour la nourriture, la saturation dessinée par-dessus en un fond plus vif ; bouteille pour l'eau ; thermomètre pour la température ; bulle pour l'air, seulement quand vous êtes sous l'eau |
| **Signes vitaux** | Goutte pour l'expérience avec le numéro de niveau à l'intérieur ; croix dorée pour l'absorption ; croix pour la santé, ou celle de votre monture quand vous êtes à cheval |

Une ligne verticale sépare les sections, et seulement quand les deux côtés en contiennent
— ainsi le séparateur des effets va et vient avec les effets eux-mêmes, tandis que celui
entre sustentation et signes vitaux est toujours là.

L'absorption est dessinée comme une **seconde croix**, marquée d'un petit plus en haut à
droite, plutôt que comme une icône à part entière — le plus sert à les distinguer.

Les icônes qui vont et viennent ne déplacent pas celles qui sont toujours là : la ligne est
alignée à droite, elle grandit et rétrécit donc à gauche.

> **L'eau et la température sont dessinées mais pas encore relevées.** L'eau imite la
> nourriture, donc elle bouge quand la nourriture bouge. La température se tient à
> mi-chemin et en blanc, ce qui est « confortable » sur un thermomètre. Ce sont deux
> substituts pour un système de soif et de température, et elles deviendront de vraies
> lectures quand un tel mod est présent.

### Bandes de couleur

La santé et la nourriture utilisent **les bandes propres à DayZ**, qui diffèrent l'une de
l'autre. La santé est rapportée à 100 PV ; la nourriture à une réserve de 5 000 points.

| | Blanc | Jaune | Rouge | Clignotant |
| :--- | :--- | :--- | :--- | :--- |
| **Santé** | 61–100 % | 31–60 % | 15–30 % | 0–14 % |
| **Nourriture** | 16–100 % | 6–15 % | 2–5 % | 0–1,9 % |

Les barres de Minecraft valent 20 points pour les deux : la santé passe au jaune à 12 ou
moins, au rouge à 6 ou moins, et clignote en dessous de 3 — tandis que la nourriture passe
au jaune à 3 ou moins, au rouge à 1, et ne clignote que quand elle est vide. La nourriture
est délibérément la plus indulgente des deux : dans DayZ, l'avertissement de la faim arrive
bien plus tard que celui de la perte de sang.

![La lecture de statut au niveau critique : la pomme de nourriture, la bouteille d'eau et la croix de santé clignotent toutes en rouge, tandis que le cœur d'effet bénéfique et la goutte d'expérience restent blancs](docs/screenshots/uwu-icons.gif)

La bande critique en mouvement — nourriture, eau et santé clignotent toutes en rouge à
vide. Le cœur d'effet bénéfique à gauche et la goutte d'expérience restent blancs pendant
tout cela, parce qu'un effet est soit actif, soit inactif et que la distance parcourue dans
un niveau n'est pas un avertissement.

L'air n'a pas de bandes propres et emprunte celles de la santé, puisque la noyade et le
saignement sont le même type d'urgence. L'absorption et l'expérience ne sont
volontairement **pas** hiérarchisées par couleur : avoir moins d'absorption n'est pas un
avertissement, et non plus la distance jusqu'au prochain niveau.

### Marqueurs de tendance

Un chevron empilé indique dans quelle direction chaque statistique évolue — **au-dessus**
de l'icône quand elle monte, **en dessous** quand elle descend, de sorte que le marqueur
soit du côté où la valeur se dirige.

- **Un chevron** — dérive ordinaire
- **Deux chevrons** — un changement significatif, ce qui fait qu'un tick de poison ou un
  effet de régénération se lit différemment d'une faim qui descend toute seule

Il est maintenu une seconde et demie après l'arrêt du mouvement, puis s'estompe — assez
longtemps pour qu'un bref échange de dégâts ne vienne et reparte avant que vous ne le
voyiez. Les seuils sont par statistique et mesurés sur une seconde, et volontairement bas
pour les statistiques qui bougent lentement : la régénération naturelle de la santé n'est
que d'environ 0,25 PV par seconde, donc un seuil de 1,0 ne se déclencherait jamais et vous
ne verriez jamais que vous vous soignez.

La température n'affiche jamais de marqueur. Sa flèche pointerait une direction sur
laquelle vous ne pouvez pas agir, et la lecture qu'elle portera finalement est un niveau
plutôt qu'une tendance.

### Le panneau du joueur

Le coin en bas à gauche porte un panneau de deux lignes sur le même fond que les
emplacements de la hotbar.

- **Rangée du haut** — l'objet tenu : un point d'état DayZ (neuf, usé, endommagé, très
  endommagé, ruiné) et son nom. La moitié droite est laissée volontairement vide pour le
  mode de tir, la portée et les munitions d'une arme.
- **Rangée du bas** — une figure de posture (marche, sprint ou accroupi), une marque de
  bouclier, et une barre d'armure.

![Le panneau du joueur en bas à gauche montrant un point d'état neuf à côté du nom Diamond Pickaxe, avec une figure de posture marchant et une barre d'armure partiellement remplie sur la rangée du dessous](docs/screenshots/hud-held-tool.png)

### Marques d'effet

Les effets de potion actifs sont réduits à **une marque par famille** plutôt qu'une par
effet, parce que Minecraft a une trentaine d'effets et qu'une ligne qui grandirait par
effet mangerait le bord de l'écran. Ce qui compte d'un coup d'œil, c'est quels *types* de
choses sont sur vous.

| Marque | Famille |
| :--- | :--- |
| Cœur | Bénéfique — vitesse, force, vision nocturne |
| Pilule | Régénérant — régénération, absorption, saturation |
| Cœur brisé | Nuisible — poison, faim, fatigue d'extraction, tout le reste de mauvais |

Elles sont dessinées en blanc sans niveau de remplissage ni bandes de couleur, parce qu'un
effet est soit actif, soit inactif. Quand l'un se termine, sa marque **s'estompe** sur une
seconde plutôt que de disparaître.

![La lecture de statut garnie de plusieurs marques d'effet de potion à la fois, à côté des icônes de nourriture, d'eau, de température, d'expérience, de santé et d'absorption](docs/screenshots/hud-effect-marks.png)

### Dessiné à la main, pas texturé

Chaque icône est un pixel art défini dans le source sur une grille 15x15 plutôt que chargée
depuis une texture. Le mod ne livre aucun visuel d'icône propre, il ne peut donc pas
entrer en conflit avec un pack de ressources.

---

## Installation

Choisissez le jar pour votre version de Minecraft et votre chargeur. Le nom de fichier
porte les deux, p. ex. `dayz-hotbar-fabric-1.21.1-1.2.0.jar`. Chaque build dessine le même
HUD, mais ils sont construits pour des versions et chargeurs différents et ne sont **pas**
interchangeables.

1. Installez le chargeur correspondant à votre version du jeu —
   [Fabric Loader](https://fabricmc.net/use/),
   [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) ou
   [NeoForge](https://neoforged.net/).
2. Déposez le jar correspondant dans votre dossier `mods`.

Les téléchargements sont sur la [page des releases](https://github.com/aacanadaa/DayZ-Hotbar/releases),
et sur les deux stores sous la version que vous utilisez.

Aucun chargeur n'a besoin d'un mod d'API — pas de Fabric API, et rien de supplémentaire
côté Forge ou NeoForge. Forge et NeoForge sont des téléchargements séparés même s'ils se
resssemblent : ce sont des chargeurs différents avec des API HUD différentes, et les jars
ne sont pas interchangeables.

---

## Dépendances

| | Exigence |
| :--- | :--- |
| Version du mod | 1.2.0 (une seule source couvre 1.20.1 – 26.3) |
| Fabric | Un Fabric Loader correspondant à votre version du jeu |
| Forge | Une ligne Forge qui fournit l'API de couche HUD, correspondant à votre version du jeu |
| NeoForge | 1.20.6 ou plus récent |
| Java | 21+ sur 1.20.5+ ; 17 sur 1.20.1–1.20.4 ; 25 sur 26.x |
| Fabric API | Non requis |
| Mods d'API Forge / NeoForge | Non requis |

---

## Notes

- Les éléments vanilla de santé, faim, armure, air et expérience sont supprimés plutôt que
  redessinés par-dessus, donc rien ne se double.
- Les règles de visibilité propres à vanilla sont héritées : le HUD se cache toujours
  derrière un écran ouvert, en mode spectateur, et quand vous appuyez sur F1.
- L'indicateur de force d'attaque vanilla vivait dans la hotbar, donc remplacer la hotbar
  le supprime. Il n'est pas réimplémenté — définissez **Options → Paramètres vidéo →
  Indicateur d'attaque** sur *Viseur* si vous voulez le récupérer.
- **Sur Forge 1.20.6 et 1.21.1–1.21.5**, deux petits éléments vanilla partent avec : la
  brève popup du « nom de l'objet sélectionné », et la barre de charge de saut à cheval.
  Ces lignes Forge gardent la rangée d'emplacements, la barre d'expérience, la rangée de
  santé et la santé de la monture dans une seule couche, donc il n'y a rien de plus fin à
  laisser activé.
- **À partir de 1.21.6**, vanilla a déplacé la barre d'expérience dans une « barre
  contextuelle » partagée qui contient aussi le compteur de saut de la monture et la barre
  de localisation. Remplacer la barre d'expérience supprime tout ce widget sur Fabric,
  NeoForge et (à partir de 1.21.8) Forge : la lecture DayZ dessine toujours sa propre
  icône d'expérience, mais les barres vanilla de localisation et de saut ne sont pas
  redessinées. Sur 1.20.1–1.21.5, les trois chargeurs les conservent.

---

## Compiler depuis les sources

Cet arbre gère toute la matrice avec
[Stonecutter](https://stonecutter.kikugie.dev/) : **une seule source**, une liste de
versions dans `settings.gradle.kts`, et un nœud de build par paire (chargeur × version du
jeu). Cela requiert **JDK 25** comme JDK de lancement — les toolchains Java 17 / 21 / 25
dont chaque version du jeu a besoin sont téléchargées à la demande par le résolveur foojay.

```bash
# Build every version and loader in the matrix
JAVA_HOME=/path/to/jdk-25 ./gradlew chiseledBuild

# Build a single node
JAVA_HOME=/path/to/jdk-25 ./gradlew :fabric:1.21.1:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :neoforge:26.2:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :forge:1.21.11:build

# List every node
./gradlew matrix
```

Les produits se trouvent dans le répertoire de build du nœud, avec la version du jeu dans
le nom de fichier :

- `fabric/versions/<mc>/build/libs/dayz-hotbar-fabric-<mc>-<version>.jar`
- `neoforge/versions/<mc>/build/libs/dayz-hotbar-neoforge-<mc>-<version>.jar`
- `forge/versions/<mc>/build/libs/dayz-hotbar-forge-<mc>-<version>.jar`

Chacun de ces fichiers est l'artefact livrable — aucun n'a besoin d'étape de
post-traitement. Un `-sources.jar` est écrit dans le même dossier, alors faites attention
de choisir le bon fichier si vous copiez à la main. Voir
[docs/BUILDING.en.md](docs/BUILDING.en.md) pour comment le build est assemblé et pourquoi
la matrice a les lacunes qu'elle a.

---

## Liens

- **Source** : <https://github.com/aacanadaa/DayZ-Hotbar>
- **Problèmes** : <https://github.com/aacanadaa/DayZ-Hotbar/issues>
- **Journal des modifications** : [CHANGELOG.md](CHANGELOG.md)
- **DayZ Inventory** : <https://github.com/aacanadaa/DayZ-Inventory>

---

## Licence et copyright

Licencié sous la [Apache License 2.0](LICENSE).

Libre d'utiliser, modifier et redistribuer — dans des modpacks, sur des serveurs, et
commercialement. La seule condition est que l'avis de copyright et une copie de la licence
accompagnent toute copie que vous transmettez.

Copyright 2026 suoim.
