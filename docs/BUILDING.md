# 构建 DayZ Hotbar

**中文** | [English](BUILDING.en.md)

这份文档说明这套统一的多版本 / 多加载器构建是怎么搭起来的、为什么是这个形状，以及怎么再加一个
Minecraft 版本。

---

## 1. 为什么是一棵树，而不是每个版本一个分支

这个项目原本每个 Minecraft 版本一个分支，每个修复都要往每个分支合并一遍，每次合并也都要单独构建
和测试。1.20.1 更是直接停在 **`v1.0.1`** 标签上，而不是一个活分支，所以在那上面修任何东西都等于
重放历史。

现在它是一个分支、一份源码树。版本相关的代码用 `//? if <条件>` 注释就地标注，构建为每个**节点**
产出一个产物 —— 一个节点就是一个（模块 × Minecraft 版本）组合，例如 `:fabric:26.2`。这棵树覆盖
**23 个 Minecraft 版本**（1.20.1 到 26.3），产出 **50 个可发布 jar**。

[Stonecutter 自己的指南](https://stonecutter.kikugie.dev/)描述了两种布局：**扁平**布局（一份
`src/`，每个"版本 × 加载器"一个节点，加载器逻辑用编译常量选择）和**分支**布局（共享一个 `common/`
加上各加载器模块，每个模块有自己的版本节点）。本项目用分支布局，因为两个加载器确实需要不同的
工具链 —— Fabric Loom 和 NeoForge ModDevGradle —— 而且它保留了代码库里本来就有的
`common/` / `fabric/` / `forge/` / `neoforge/` 划分。

[Architectury](https://docs.architectury.dev/) 曾被评估为加载器抽象层，但**没有采用**：

1. Architectury API 会成为每一份下载的硬运行时依赖。本模组是通过每个加载器**自己的原生机制**
   够到 HUD 的（Fabric 用 Mixin，Forge 和 NeoForge 用各自的层渲染器），抽象层只会多一个依赖，
   却不会消除那些本来就必须存在的按加载器写的代码。
2. 从 26.1 起 Minecraft **不再混淆**，Fabric Loom 也拆成了 `fabric-loom`（26.1+）和
   `fabric-loom-remap`（<26.1）。Architectury Loom 会在这个拆分之上再叠一层，而 26.x 上还没有
   既有先例 —— 偏偏 26.x 是最重要的那条线。

---

## 2. 目录结构

```
settings.gradle.kts          Stonecutter 树 + 插件管理 + 版本矩阵
stonecutter.gradle.kts       控制器：当前版本、chiseledBuild、publishAll、matrix
gradle.properties            模组元数据、发布 id、共享工具版本
build-logic/                 每个节点共享的约定插件
versions/<mc>/gradle.properties   每个游戏版本的依赖坐标

common/                      与加载器无关的 HUD 代码 + Fabric mixin
fabric/                      Fabric 入口
neoforge/                    NeoForge 入口 + NeoForgeHudHandler
forge/                       Forge 入口 + ForgeHudHandler
```

Stonecutter 用游戏版本给每个节点命名，并把节点放在分支目录下，所以 `:fabric:26.2` 的项目目录是
`fabric/versions/26.2/`。这些目录是构建产物，只有 `versions/<mc>/gradle.properties` 进版本控制。

---

## 3. Gradle 构建怎么拼起来

### `settings.gradle.kts`

声明这棵树和矩阵。有两处是承重的，很容易弄坏：

- **根分支是故意留空的。** 它存在（Stonecutter 总会有一个），但不列任何版本。给它版本会为每个
  Minecraft 版本创建一个没有构建脚本、没有产物的项目，控制器的发布任务也就无法排序。
- **`pluginManagement.plugins {}` 同时列出两个 Loom 风味。**
  `dev.kikugie.loom-back-compat` 会 *以编程方式* 应用
  `net.fabricmc.fabric-loom` 或 `fabric-loom-remap` 之一，而编程式
  `pluginManager.apply(id)` 是对项目自己的 buildscript 仓库解析的，不是对
  `pluginManagement.repositories`。在这里列出两个 id —— 并在 `stonecutter.gradle.kts` 里用
  `apply false` 声明 —— 才能让每个节点都解析得到。

### `build-logic/`

两个预编译约定插件：

| 插件 | 应用对象 | 职责 |
| :--- | :--- | :--- |
| `dayz-common` | 每个节点 | 版本字符串、Java 工具链、仓库、清单展开、版本改名、jar 里的许可证、生成源码接线 |
| `dayz-loader` | fabric / neoforge / forge | 共享 `common` 节点的源码、打包检查、`publishMods` |

`build-logic/src/main/kotlin/Utils.kt` 里有 `prop()` / `mc` / `branch` 访问器。注意
`stonecutter { }` 在预编译脚本插件里**不可用**，扩展要通过 `sc` 访问器拿。

### 每版本属性

`versions/<mc>/gradle.properties` 是一个 Minecraft 版本坐标的唯一声明处（加载器版本、Java 级别、
mixin 兼容级别、资源包格式、发布区间）。Stonecutter 只对登记在**根**分支上的版本读这些文件，而根
分支在这里是空的，所以 `Utils.kt` 直接读它们。

---

## 4. 按版本处理的源码

Stonecutter 预处理共享的 `src/` 树，把结果写到

```
<branch>/versions/<mc>/build/generated/stonecutter/<sourceSet>/{java,resources}
```

`dayz-common` 把 `compileJava` 指向这棵生成树，**不是**原始源码 —— 原始树里同时存在每个版本的
代码，只有生成出来的副本才会把非活动分支注释掉。

`dayz-loader` 再把 **common** 节点的生成树加到加载器自己的编译任务里，所以每个加载器 jar 里都只有
一份共享类。对 Fabric 来说，mixin refmap 只能由 Mixin 注解处理器跑在**被注解的源码**上产生，这
正是为什么编译进去的是共享源码、而不是预先编译好的 `common.jar`。

共享的 `GuiMixin` 是 Fabric 的机制，所以 NeoForge 和 Forge 会把它从编译和 jar 里**排除**：这两个
加载器通过各自的层渲染器够到 HUD，在那里加载这个 mixin 会把 HUD 画两遍。

---

## 5. 条件编译

用两种机制。

### `//? if` 注释

用于任何结构性差异 —— 不同的方法签名、不同的挂钩目标、被删掉的方法：

```java
//? if >=1.21 {
private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
//?} else {
private static void render(GuiGraphicsExtractor graphics, float partialTick) {
//?}
```

**不要在条件块里放注释** —— 处理器会重写注释标记，残留的 `//` 可能被剥掉，于是注释变成代码。

### `replacements` 批量改名

用于那些否则要在同一个标识符上包几十次 `//? if` 的纯改名：

```kotlin
sc.replacements.string(sc.current.parsed < "1.21.5") {
    replace("getInventory().getSelectedSlot()", "getInventory().selected")
}
```

源码按**最新**的名字写（`getSelectedSlot()`、`GuiGraphicsExtractor`、
`MobEffects.INSTANT_HEALTH`），改名对更老的节点**反向**应用。

**Stonecutter 的替换是双向的** —— 条件成立时正向替换，不成立时反向替换 —— 所以较长的那个拼写
不能是另一侧任何东西的子串。`MobEffects.HEAL` 是 `HEALTH_BOOST` 的前缀，朴素的
`replace("MobEffects.INSTANT_HEALTH", "MobEffects.HEAL")` 会让反向趟把新版本改成
`INSTANT_HEALTHTH_BOOST`。把两种拼写分别锚定到 `.value()` 调用或后面的逗号上，两者就不再会混淆。

---

## 6. 版本矩阵

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

合计：**23 个 Minecraft 版本、50 个产物**（23 Fabric、9 Forge、18 NeoForge）。
`./gradlew matrix` 打印权威节点列表。

### 各版本的 HUD API 分界

以下都是**打开对应版本的真实 jar 看出来的**，不是按版本号推的。

| 自 | 变化 |
| :--- | :--- |
| 1.20.2 | 仅 Fabric。`ItemCooldowns#isOnCooldown` 仍接收 `Item` |
| 1.20.5 | 效果变成 `Holder<MobEffect>`；`MobEffectInstance#getEffect()` 返回 holder。原版改用 `LayeredDraw`，快捷栏方法改为 `renderItemHotbar`；`renderExperienceLevel` 拆了出来。`Gui#render` 仍接收 `float` |
| 1.21 | `Gui#render` 和 `renderItemHotbar` 改收 `DeltaTracker` |
| 1.21.2 | `ItemCooldowns#isOnCooldown` 改收 `ItemStack`。食物、护甲和氧气改由 `Gui#render` 直接绘制，而不是从 `renderPlayerHealth` 里调，所以需要单独取消 |
| 1.21.5 | `Inventory#selected` 变为私有，改用 `getSelectedSlot()`。`MobEffects.HEAL` 改名为 `INSTANT_HEALTH` |
| 1.21.6 | GUI 矩阵栈换成 `Matrix3x2f`（`pushPose`→`pushMatrix`，三参 `translate`/`scale` 去掉 z）。经验条变成"上下文条"，整个底部区块由 `renderHotbarAndDecorations` 一次画完 |
| 1.21.9 | `Level#isClientSide` 变私有（本模组未用） |
| 1.21.11 | `ResourceLocation` 改名为 `Identifier` |
| 26.1 | Minecraft 不再混淆：没有映射、没有 refmap、没有 remap 步骤。`GuiGraphics` → `GuiGraphicsExtractor`，所有 `render*` → `extract*`，`drawString`→`text`，`renderItem`→`item`，`renderItemDecorations`→`itemDecorations` |
| 26.2 | HUD 从 `Gui` 搬进新的 `Hud` 类；`getGuiTicks()` 和隐藏标志跟着搬，`Options.hideGui` 变成 `Hud.isHidden()` |
| 26.3 | `InputConstants.Type.KEYSYM` → `KEYBOARD`（本模组未用） |

---

## 7. 已启用的目标与仍存在的空缺

### Forge 1.20.1

不构建。Forge 1.20.1 跑在 SRG 名字上，需要 reobfuscation 加 Searge mixin refmap。ForgeGradle 7
两者都没有，而有的 ForgeGradle 6 只支持 Gradle 8，Loom 1.18.1 却需要 Gradle 9。1.20.1 仍然有
Fabric 构建。

### Forge 1.21、1.21.6、1.21.7

不构建。检查 51.0.33、56.0.9、57.0.3 的 Forge universal jar，里面**没有**
`ForgeLayeredDraw`，也没有 `AddGuiOverlayLayersEvent` —— Forge 那几条线正在重写、根本没发布可用的
HUD 挂钩，所以没有能挂的东西。1.21.6 和 1.21.7 由 NeoForge 覆盖，1.21 由 Fabric 覆盖。

### Forge 1.21.2

不构建：Forge 没有发布这个版本。

### Forge 26.x

不构建：这里没有可用的 Forge 26.x 线；26.x 走 NeoForge。

### 1.20.6 以下的 NeoForge

不构建。NeoForge 于 1.20.2 从 Forge 分叉，其 20.2.x–20.5.x 线早于本模组依赖的
`RenderGuiLayerEvent` 层挂钩。1.20.1 更是早于 NeoForge 本身。1.20.1 起由 Fabric 覆盖。

Fabric 和 NeoForge 合起来覆盖 1.20.6 以上的所有版本，1.20.1–1.20.5 则由 Fabric 单独覆盖。

---

## 8. 新增一个 Minecraft 版本

1. 加 `versions/<mc>/gradle.properties`，照抄最接近的版本并更新 `deps.minecraft`、`deps.java`、
   `deps.mixin-compat`、`deps.pack-format`、加载器版本和 `meta.minecraft-range`。
2. 把版本加进 `settings.gradle.kts` 里对应的列表。
3. 跑 `./gradlew :common:<mc>:compileJava` 并移植报错的地方。只有当该 Forge 线提供层 API 时
   （见第 7 节）才把它加进 `forgeVersions`。
4. 把版本加进 `README.md` / `README.en.md` 和 `docs/store-descriptions/` 里的商店文案。
5. `./gradlew chiseledBuild` 确认整个矩阵仍然能构建。

发布会自动带上新版本：游戏版本标签和加载器标签是从节点读的，不是每次发布手写的。

---

## 9. 发布

`dayz-loader` 为每个加载器节点配置 `me.modmuss50.mod-publish-plugin`：

- Modrinth 项目 `hsTmy4Hi`，CurseForge 项目 `1693963`。
- token 来自环境变量：`MODRINTH_TOKEN`（回退 `MODRINTH_PAT`）和 `CURSEFORGE_API_KEY`
  （回退 `CURSEFORGE_TOKEN`）。
- `publish.dry_run` 默认 `true`，所以误跑的 `publishMods` 不会提交任何东西。
- `publishAll` 依赖每个节点的 `publishMods`，并做了排序，避免 CurseForge 收到突发上传而限流。

这个模组在两个平台上都**不声明任何依赖** —— 它不用 Fabric API，也不需要加载器 API 模组 —— 所以
不会让用户去装多余的东西。

**CurseForge 从不返回 URL。** 每次上传都进入人工审核；API 接收文件后就返回。所以
`publishCurseforge` 变绿只代表*已提交*。Modrinth 是立即返回的。

CurseForge 的**项目描述**无法从构建里更新 —— 上传 token 是只读上传的旧版 token，能改元数据的
Eternal API 会拒绝它。`docs/store-descriptions/curseforge.md` 是该页面的复制粘贴源，需要手动保持
同步。Modrinth 的描述*会*通过 API 推送。

---

## 10. 坑

1. **`gradlew` 必须保持可执行**（权限 `100755`）。CI 也会跑 `chmod +x ./gradlew`。
2. **Gradle wrapper 是 9.7.0。** Fabric Loom 1.18.1 发布的是
   `org.gradle.plugin.api-version = 9.7.0`；更老的 wrapper 会用变体匹配错误失败，而错误里不会提
   Gradle 版本。
3. **不要硬编码 `org.gradle.java.home`。** 它是机器相关的，会弄坏 CI。启动 JDK 通过 `JAVA_HOME`
   提供；各版本工具链来自 `versions/<mc>/gradle.properties`，由 foojay 解析器下载。
4. **`prop()` 读两个来源。** 先 `findProperty`（所以 `-P` 和根 `gradle.properties` 优先），再
   `versions/<mc>/gradle.properties`。
5. **绝不要在 `//? if` 块里写注释。**
6. **目标被改名时 mixin 不是降级，而是崩溃。** 客户端 mixin 配置是 `required: true`。目标一动，就
   grep 出所有调用点，挂钩所有调用都经过的那个方法；不要猜。
7. **替换是双向的。** 让两种拼写互不为子串，才能保证反向趟不会改坏新版本。
8. **Forge 的层树变过形状。** 1.20.6–1.21.5 是 `PRE_SLEEP_STACK` 下的 `HOTBAR` + `EXPERIENCE`；
   1.21.8–1.21.10 是单一的 `HOTBAR_AND_DECOS`；1.21.11 是 `ITEM_HOTBAR` + `HEALTH_BAR` +
   `VEHICLE_HEALTH` + `EXPERIENCE_LEVEL` + `CONTEXTUAL_INFO`。
9. **1.21.6 把 Forge 移到 EventBus 7**，它把 `net.minecraftforge.eventbus.api` 拆成 `bus` 和
   `listener`；从该版本起 `@SubscribeEvent` 来自
   `net.minecraftforge.eventbus.api.listener`。
