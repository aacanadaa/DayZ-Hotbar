# DayZ Hotbar

**中文** | [English](README.en.md)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-hotbar?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-hotbar)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1693963?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-hotbar)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%7C%20...%20%7C%2026.3-62b47a.svg)
![Loader](https://img.shields.io/badge/Loader-Fabric%20%7C%20Forge%20%7C%20NeoForge-dbb69b.svg)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Hotbar?color=red)](https://github.com/aacanadaa/DayZ-Hotbar/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-%E8%B5%9E%E5%8A%A9%E6%88%91-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

把 Minecraft 的 HUD 换成 **DayZ** 风格的快捷栏（hotbar）和 DayZ 风格的状态读数，视觉上与
[DayZ Inventory](https://github.com/aacanadaa/DayZ-Inventory) 保持一致。

**支持 Minecraft 1.20.1 到 26.3，一份源码、三个加载器。任何加载器都不需要前置 API 模组。**

## 支持的版本

整个版本矩阵来自同一份源码树，用 [Stonecutter](https://stonecutter.kikugie.dev/) 按版本条件编译，
为每个（加载器 × 游戏版本）组合产出一个 jar。

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

Forge 没有 1.21、1.21.6、1.21.7 三个版本：这几条 Forge 线没有可用的 HUD 层 API，没有能挂钩的东西。
Forge 也没有 1.21.2（官方就没发布）和 26.x。完整的取舍记录在
[docs/BUILDING.md](docs/BUILDING.md)。

每个组合产出的 HUD 画出来的东西是同一套，但 jar **不能混用** —— 请按你的游戏版本和加载器选。

> **提示 —— GUI 缩放。** HUD 按固定像素尺寸排版，并以 Minecraft 默认的 *自动* GUI 缩放为基准调过。
> GUI 缩放调**大**时，快捷栏、玩家面板和状态读数会被挤到一起，甚至可能在屏幕中间撞上；调得**很小**
> 时图标会看不清。出现任一情况，请调整 **选项 → 视频设置 → GUI 缩放**。

![游戏中的 DayZ Hotbar HUD：左下是手持物品面板（空手），中间是九格快捷栏、当前手持格发绿，右下是状态读数，显示食物、水、体温、经验和生命](docs/screenshots/uwu.png)
![DayZ Inventory 界面 —— Vicinity 网格里打开了 Jukebox 抽屉，Survivor 面板、显示 Decorated Pot 的 2.0x Hands 槽，以及 2x2 合成网格](https://cdn.modrinth.com/data/8asZxzdc/images/66b7282b83c958bd63ec912c7353bb4817bc202a.png)

上图是搭配背包模组的效果 ^^

---

## 功能

### 快捷栏

九格加上副手，排成平直的一行，用的是和 DayZ Inventory 界面同一套近黑色半透明样式。每个灰框是
22x22，框与框之间只留 1 像素空隙；整体没有底板，也没有任何描边 —— 槽位状态完全靠色块的颜色来传达。

| 状态 | 含义 |
| :--- | :--- |
| 暗淡色块 | 空 |
| 提亮色块 | 有物品 |
| 绿色块 | 当前手持的槽位 |
| 红色块 | 手持中，但物品在冷却、无法使用 |

切换槽位有动画：新选中的格子先亮成**黄色**，再用约三分之一秒过渡到**绿色**，所以一次换手看起来是
一个事件，而不是瞬间翻面。

![快捷栏里装着一把剑、一把镐、一组牛排、一支火把和一组金苹果，手持的牛排格发绿，堆叠的格子上画着物品数量](docs/screenshots/hud-full-hotbar.png)

### 状态读数

右下角一排横向图标，和快捷栏共用同一条边距，让整个 HUD 在屏幕上读起来是一条线。每个图标都画成一个
**容器** —— 一圈轮廓、隔一格留白，内部随着数值上升从底部往上填。轮廓用的是和填充一样的颜色，所以
黄色图标的轮廓也是黄的。

这一行从左到右分成三段：

| 段 | 图标 |
| :--- | :--- |
| **效果** | 每一类生效中的药水效果一个标记 |
| **生存** | 食物用苹果表示，饱和度以更亮的色块叠画在苹果上；水是瓶子；体温是温度计；气泡表示氧气，只在你水下时出现 |
| **生命体征** | 经验是一滴水滴，等级数字画在里面；伤害吸收是金色十字；生命是十字，骑乘时改为坐骑的生命 |

各段之间用一条竖线隔开，而且只在两边都有东西时才画 —— 所以效果那段的分隔线会随效果本身来来去去，
而生存与生命体征之间那条是一直都在的。

伤害吸收画成**第二个十字**，右上角带一个加号小徽标，而不是单独设计一个图标 —— 加号就是用来区分
两者的。

时有时无的图标不会把常驻图标挤来挤去：这一行是右对齐的，所以它的增减发生在左侧。

> **水和体温目前只画、还没接数据。** 水跟着食物走，所以食物一动它就动。体温停在白色、刻度一半的
> 位置，在温度计上表示"舒适"。这两个都是口渴与体温系统的占位，等装了对应的模组就会变成真实读数。

### 颜色分段

生命和食物用的是 **DayZ 自己的分段**，两者并不相同。生命按 100 HP 折算，食物按 5,000 点储备折算。

| | 白色 | 黄色 | 红色 | 闪烁 |
| :--- | :--- | :--- | :--- | :--- |
| **生命** | 61–100% | 31–60% | 15–30% | 0–14% |
| **食物** | 16–100% | 6–15% | 2–5% | 0–1.9% |

Minecraft 两条槽都是 20 点，所以生命在 12 点及以下变黄、6 点及以下变红、低于 3 点闪烁；而食物在
3 点及以下变黄、1 点变红、只在空的时候闪烁。食物是故意更宽容的那一个：在 DayZ 里，饥饿的警告来得
比失血晚得多。

![临界状态下的状态读数：食物苹果、水瓶和生命十字都在闪红，而左侧的增益效果心形和经验水滴仍是白色](docs/screenshots/uwu-icons.gif)

动起来看临界段 —— 食物、水和生命都在空值下闪红。左侧的增益效果心形和经验水滴全程仍是白色，因为
效果只有开和关，而你在等级里走了多远也不是警告。

氧气没有自己的分段，它借用生命的分段，毕竟溺水和失血是同一类紧急情况。伤害吸收和经验是刻意**不
分段配色**的：吸收少不是警告，离升级差多少也不是。

### 趋势标记

一枚 V 形叠标显示每项数值的走向 —— 上升时在图标**上方**，下降时在**下方**，标记始终落在数值要去
的那一侧。

- **一个 V** —— 普通漂移
- **两个 V** —— 显著变化，正是它让中毒掉血或再生效果和单纯的自然饥饿下降读起来不一样

停止变化后它还会停留一秒半再淡出 —— 长到一次短暂的交火不会在你看见之前就来了又走。阈值是按每项
数值分别定的，以一秒为窗口测量，而且对变化慢的数值刻意定得很低：自然回血每秒只有约 0.25 HP，
阈值定 1.0 就永远不会触发，你也就永远看不到自己在回血。

体温永远不显示标记。它的箭头会指向一个你无法干预的方向，而且它将来承载的读数是一个水平值，不是
一个趋势。

### 玩家面板

左下角是一块两行的面板，和快捷栏的槽位用同一层色块。

- **上排** —— 手持物品：一个 DayZ 状态圆点（崭新、磨损、损坏、严重损坏、报废）和物品名。右半边是
  刻意留空的，留给武器的开火模式、射程和弹药。
- **下排** —— 一个姿态小人（行走、奔跑或潜行）、一个盾牌标记，以及一条护甲槽。

![左下角的玩家面板：Diamond Pickaxe 名字旁边是崭新的状态圆点，下面一行是行走姿态小人和一条部分填充的护甲槽](docs/screenshots/hud-held-tool.png)

### 效果标记

生效中的药水效果会归并成**每一类一个标记**，而不是每个效果一个，因为 Minecraft 有三十多种效果，
按效果增长的一行会把屏幕边缘吃掉。一眼看过去真正重要的是你身上挂了哪几*类*东西。

| 标记 | 类别 |
| :--- | :--- |
| 心形 | 增益 —— 速度、力量、夜视 |
| 药丸 | 恢复 —— 再生、伤害吸收、饱和 |
| 碎裂的心 | 负面 —— 中毒、饥饿、挖掘疲劳，以及其它所有不好的 |

它们一律画成白色，没有填充度、没有分段配色，因为效果只有开和关。某个效果结束时它的标记会用一秒
**淡出**，而不是直接消失。

![状态读数上同时挂着好几个药水效果标记，紧挨着食物、水、体温、经验、生命和伤害吸收图标](docs/screenshots/hud-effect-marks.png)

### 手绘，不是贴图

所有图标都是在源码里按网格定义的像素画，而不是从贴图加载的。模组本身不带任何图标美术，所以它不会
和资源包冲突。

---

## 安装

按你的 Minecraft 版本和加载器挑对应的 jar。文件名里已经带了游戏版本和加载器，例如
`dayz-hotbar-fabric-1.21.1-1.2.0.jar`。所有构建画出来的 HUD 都一样，但它们是为不同的游戏版本和加载器
构建的，**不能互换**。

1. 装好对应游戏版本的加载器 —— [Fabric Loader](https://fabricmc.net/use/)、
   [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) 或 [NeoForge](https://neoforged.net/)。
2. 把对应 jar 放进 `mods` 文件夹。

下载在 [releases 页面](https://github.com/aacanadaa/DayZ-Hotbar/releases)，两个平台上也在对应你游戏
版本的条目下。

任何加载器都不需要前置 API 模组 —— 不需要 Fabric API，Forge 或 NeoForge 那边也不需要额外装什么。
Forge 和 NeoForge 虽然长得像，但是两份独立的下载：它们是不同的加载器，HUD API 也不同，jar 不能
互换。

---

## 依赖

| | 要求 |
| :--- | :--- |
| 模组版本 | 1.2.0（同一份源码覆盖 1.20.1 – 26.3） |
| Fabric | 与游戏版本匹配的 Fabric Loader |
| Forge | 与游戏版本匹配且带 HUD 层 API 的 Forge |
| NeoForge | 1.20.6 或更新 |
| Java | 1.20.5+ 需 21 或更新；1.20.1–1.20.4 需 17；26.x 需 25 |
| Fabric API | 不需要 |
| Forge / NeoForge API 模组 | 不需要 |

---

## 说明

- 原版的健康、饥饿、护甲、氧气和经验元素是被**关掉**的，而不是画在它们上面，所以不会有任何重影。
- 原版的可见性规则照旧继承：HUD 依然会在打开界面时、旁观模式下和按下 F1 时隐藏。
- 原版的攻击力度指示器原本画在快捷栏里，替换掉快捷栏就把它一并去掉了。这里没有重新实现它 ——
  想要的话，把 **选项 → 视频设置 → 攻击指示器** 设成*准星*。
- **在 Forge 1.20.6 与 1.21.1–1.21.5 上**，还有两个小的原版元素会跟着一起消失：短暂的"选中物品名称"
  弹窗，以及骑马时的跳跃蓄力条。这几条 Forge 把槽位行、经验条、生命行和坐骑生命放在同一层里，所以
  没有更细的东西可以单独留着。
- **从 1.21.6 起**，原版把经验条移进了一个共享的"上下文条"，坐骑跳跃条和定位条也住在里面。替换经验条
  会把整个上下文条一并去掉（Fabric、NeoForge，以及 1.21.8 起的 Forge）：DayZ 读数仍然自己画经验图标，
  但原版的定位条和跳跃条不会重画。1.20.1–1.21.5 三个加载器都保留它们。

---

## 从源码构建

这份源码树用 [Stonecutter](https://stonecutter.kikugie.dev/) 管理整个版本矩阵：**一份源码**，一份
`settings.gradle.kts` 里的版本清单，每个（加载器 × 游戏版本）组合是一个构建节点。需要 **JDK 25**
作为启动 JDK —— 各游戏版本所需的 17 / 21 / 25 工具链会由 foojay 解析器按需下载。

```bash
# 构建矩阵里的每一个版本和加载器
JAVA_HOME=/path/to/jdk-25 ./gradlew chiseledBuild

# 只构建一个节点
JAVA_HOME=/path/to/jdk-25 ./gradlew :fabric:1.21.1:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :neoforge:26.2:build
JAVA_HOME=/path/to/jdk-25 ./gradlew :forge:1.21.11:build

# 列出全部节点
./gradlew matrix
```

产物在对应节点的构建目录里，文件名已经带上游戏版本：

- `fabric/versions/<mc>/build/libs/dayz-hotbar-fabric-<mc>-<版本>.jar`
- `neoforge/versions/<mc>/build/libs/dayz-hotbar-neoforge-<mc>-<版本>.jar`
- `forge/versions/<mc>/build/libs/dayz-hotbar-forge-<mc>-<版本>.jar`

这些就是可直接发布的产物，不需要任何后处理步骤。同一目录下还会写出 `-sources.jar`，手动拷贝时注意
别拿错。构建细节和版本矩阵的取舍见 [docs/BUILDING.md](docs/BUILDING.md)。

---

## 链接

- **源码**：<https://github.com/aacanadaa/DayZ-Hotbar>
- **问题反馈**：<https://github.com/aacanadaa/DayZ-Hotbar/issues>
- **更新日志**：[CHANGELOG.md](CHANGELOG.md)
- **DayZ Inventory**：<https://github.com/aacanadaa/DayZ-Inventory>

---

## 许可证与版权

以 [Apache License 2.0](LICENSE) 授权。

可自由使用、修改和再分发 —— 包括放进整合包、放在服务器上和商用。唯一的条件是：你转发的每一份副本
都要带上版权声明和一份许可证。

Copyright 2026 suoim.
