/*
 * DayZ Hotbar - `dayz-common` convention plugin
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Applied to every Stonecutter node (common / fabric / forge / neoforge).
 * It owns the parts that do not depend on which mod loader is being built:
 * version strings, the Java toolchain, repositories, resource expansion and the
 * licence jar.
 */

import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

val minecraftVersion = mc
val javaVersion = prop("deps.java").toInt()

group = prop("mod.group")
version = "${prop("mod.version")}+mc$minecraftVersion"

// `dayz-hotbar-fabric-26.2`, `dayz-hotbar-neoforge-1.21.1`, ...
// The Minecraft version is part of the filename on purpose: both CurseForge and
// Modrinth reject a second file whose display name collides inside a project,
// and this mod ships the same mod version for several game versions.
extensions.configure<BasePluginExtension> {
    archivesName.set("$modSlug-$branch-$minecraftVersion")
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
    // Keep parameter names so Mixin can resolve `@Local`/`@Inject` signatures
    // that rely on them.
    options.compilerArgs.add("-parameters")
}

// ---------------------------------------------------------------------------
// Version renames
// ---------------------------------------------------------------------------
// Several versions renamed widely used members. The shared sources are written
// against the newest names, so older nodes get them rewritten backwards here
// instead of every affected line being duplicated behind a `//? if` block. Each
// block below is scoped to the release where that rename actually happened.
// `stonecutter { }` is only a DSL shorthand on scripts the plugin generates for;
// inside a precompiled convention plugin the extension has to be reached through
// the `sc` accessor from Utils.kt.
val versionRenames = sc.current.parsed < "26.1"

// 26.1 rewrote the GUI as a render-state pipeline: `GuiGraphics` became
// `GuiGraphicsExtractor`, and the immediate draw calls were renamed -
// `drawString` -> `text`, `renderItem` -> `item`, `renderItemDecorations` ->
// `itemDecorations`. These are pure renames, so they are rewritten backwards for
// everything below 26.1. (26.1 is the first unobfuscated release and already has
// the new GUI, which is why the boundary is 26.1 and not 26.2.)
//
// The shared sources are therefore written against the *newest* names, so the
// newest target is never rewritten at all.
sc.replacements.string(versionRenames) {
    replace("GuiGraphicsExtractor", "GuiGraphics")
    replace("graphics.text(", "graphics.drawString(")
    replace("graphics.item(", "graphics.renderItem(")
    replace("graphics.itemDecorations(", "graphics.renderItemDecorations(")
}

// The GUI stack swapped to Matrix3x2f in 1.21.6: pushPose/popPose became
// pushMatrix/popMatrix and the three-argument translate/scale lost their z
// component. 1.21.5 still has the old PoseStack.
sc.replacements.string(sc.current.parsed < "1.21.6") {
    replace("pose().pushMatrix()", "pose().pushPose()")
    replace("pose().popMatrix()", "pose().popPose()")
    replace("pose().translate(labelX, labelY)", "pose().translate(labelX, labelY, 0.0F)")
    replace("pose().scale(LEVEL_TEXT_SCALE, LEVEL_TEXT_SCALE)", "pose().scale(LEVEL_TEXT_SCALE, LEVEL_TEXT_SCALE, 1.0F)")
}

// Inventory#getSelectedSlot replaced the plain `selected` field in 1.21.5, when
// the field was made private. The same release renamed `MobEffects.HEAL` to
// `INSTANT_HEALTH`. The shared sources use the newest names.
//
// Stonecutter runs a replacement in BOTH directions - forwards when the condition
// holds, backwards when it does not - so the longer of the two strings must not be
// a superstring of anything the other side contains. `MobEffects.HEAL` is a prefix
// of `HEALTH_BOOST`, which turned the reverse pass into
// `INSTANT_HEALTHTH_BOOST`; anchoring each spelling to the `.value()` call or the
// trailing comma keeps the two unambiguous.
sc.replacements.string(sc.current.parsed < "1.21.5") {
    replace("getInventory().getSelectedSlot()", "getInventory().selected")
    replace("inventory.getSelectedSlot()", "inventory.selected")
    replace("MobEffects.INSTANT_HEALTH.value()", "MobEffects.HEAL.value()")
    replace("MobEffects.INSTANT_HEALTH,", "MobEffects.HEAL,")
}

// ItemCooldowns#isOnCooldown took the Item below 1.21.2 and the ItemStack from
// 1.21.2 on. Note the boundary is earlier than the 1.21.5 pair above - it is not
// the same release, which is exactly the kind of thing that is worth reading off
// the jar rather than assuming.
sc.replacements.string(sc.current.parsed < "1.21.2") {
    replace("isOnCooldown(stack)", "isOnCooldown(stack.getItem())")
}

// 1.21.11 renamed ResourceLocation to Identifier. The shared HUD code does not
// name the type - only the loader handlers do, in their layer ids.
sc.replacements.string(sc.current.parsed < "1.21.11") {
    replace("Identifier", "ResourceLocation")
}

// 26.2 moved the HUD out of `Gui` into a new `Hud` class; the GUI tick counter
// and the hidden flag moved with it. Write the newest access path and rewrite it
// back below 26.2.
sc.replacements.string(sc.current.parsed < "26.2") {
    replace("minecraft.gui.hud.getGuiTicks()", "minecraft.gui.getGuiTicks()")
    replace("minecraft.gui.hud.isHidden()", "minecraft.options.hideGui")
}

// ---------------------------------------------------------------------------
// Repositories
// ---------------------------------------------------------------------------
repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.blamejared.com/") { name = "BlameJared" }
    maven("https://maven.theillusivec4.top/") { name = "Illusive Soulworks" }
    maven("https://maven.ladysnake.org/releases") { name = "Ladysnake Libs" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
}

// ---------------------------------------------------------------------------
// Manifest expansion
// ---------------------------------------------------------------------------
// Every generated manifest (`fabric.mod.json`, both `mods.toml`s, `pack.mcmeta`
// and the mixin configs) is expanded from the same map, so a version bump or a
// new Minecraft target can never leave a stale hardcoded value behind. A stale
// `version` already shipped once: a 1.4.1 jar reported itself as 1.4.0 in crash
// reports because the toml hardcoded it.

/**
 * Convert a Maven range of the single half-open shape the version properties
 * use (`[lower,upper)`, or `[lower,)` for no upper bound) into the
 * space-separated comparison form that Fabric understands.
 *
 * This exists because **fabric-loader's version predicate parser does not
 * accept Maven bracket ranges at all**. `VersionPredicateParser.parse("[1.0.0,2.0.0]")`
 * yields a predicate whose `toString()` is the literal string `[1.0.0,2.0.0]`,
 * and which fails to match even `2.0.0` - the brackets are not syntax to it.
 * Shipping a bracket range therefore produces a mod that cannot load, with a
 * self-contradictory message ("requires [1.21.5,1.21.6) ... but only the wrong
 * version is present: 1.21.5").
 *
 * NeoForge and Forge *do* use Maven ranges, so their `mods.toml` files keep
 * using the unconverted `minecraft_range` / `forge_range` values.
 */
fun mavenRangeToFabricPredicate(range: String): String {
    val trimmed = range.trim()
    require(trimmed.startsWith("[") && trimmed.endsWith(")")) {
        "meta.minecraft-range must look like [lower,upper) - got '$range'"
    }
    val parts = trimmed.substring(1, trimmed.length - 1).split(",").map { it.trim() }
    require(parts.size == 2 && parts[0].isNotEmpty()) {
        "meta.minecraft-range must have a lower bound - got '$range'"
    }
    return if (parts[1].isEmpty()) ">=${parts[0]}" else ">=${parts[0]} <${parts[1]}"
}

val expandProps: Map<String, String> = buildMap {
    fun putIfPresent(key: String, value: String?) {
        if (!value.isNullOrBlank()) put(key, value)
    }

    put("version", prop("mod.version"))
    put("mod_id", prop("mod.id"))
    put("mod_id_slug", modSlug)
    put("mod_name", prop("mod.name"))
    put("mod_group", prop("mod.group"))
    put("mod_author", prop("mod.author"))
    put("mod_license", prop("mod.license"))
    put("mod_description", prop("mod.description"))
    put("mod_github", prop("mod.github"))
    put("mod_sources", prop("mod.sources"))
    put("mod_issues", prop("mod.issues"))
    put("minecraft_version", minecraftVersion)
    // Maven range - correct for the NeoForge and Forge `mods.toml` files.
    put("minecraft_range", prop("meta.minecraft-range"))
    // Fabric needs the comparison form; a Maven range makes the mod unloadable.
    put("minecraft_range_fabric", mavenRangeToFabricPredicate(prop("meta.minecraft-range")))
    put("java_version", javaVersion.toString())
    put("java_range", "[$javaVersion,)")
    put("java_range_fabric", ">=$javaVersion")
    put("mixin_compat", prop("deps.mixin-compat"))
    put("pack_format", prop("deps.pack-format"))

    // Loader ranges: an explicit `meta.<loader>-range` wins, otherwise the exact
    // dependency version is used as the floor. Deriving them here means a version
    // bump in `versions/<mc>/gradle.properties` cannot leave a stale range in a
    // generated manifest.
    putIfPresent("fabric_loader_version", propOrNull("deps.fabric-loader"))
    putIfPresent(
        "fabric_loader_range",
        propOrNull("meta.fabric-loader-range") ?: propOrNull("deps.fabric-loader")?.let { ">=$it" }
    )
    putIfPresent("fabric_api_version", propOrNull("deps.fabric-api"))
    putIfPresent("fabric_api_range", propOrNull("meta.fabric-api-range") ?: "*")
    putIfPresent("neoforge_version", propOrNull("deps.neoforge"))
    putIfPresent(
        "neoforge_range",
        propOrNull("meta.neoforge-range") ?: propOrNull("deps.neoforge")?.let { "[$it,)" }
    )
    putIfPresent("forge_version", propOrNull("deps.forge"))
    putIfPresent(
        "forge_range",
        propOrNull("meta.forge-range") ?: propOrNull("deps.forge")?.let { "[$it,)" }
    )
    // LegacyForge's loader version is the Forge major (e.g. 47 for 1.20.1).
    putIfPresent("forge_loader_range", propOrNull("deps.forge")?.substringBefore('.')?.let { "[$it,)" })
}

// `expand` only tolerates the tokens listed above; anything else aborts the
// build, which is the behaviour we want for a manifest placeholder.
val expandTargets = listOf(
    "fabric.mod.json",
    "META-INF/mods.toml",
    "META-INF/neoforge.mods.toml",
    "pack.mcmeta",
    "*.mixins.json",
    "*.client.mixins.json",
)

tasks.named<ProcessResources>("processResources") {
    inputs.properties(expandProps)
    filesMatching(expandTargets) {
        expand(expandProps)
    }
}

// ---------------------------------------------------------------------------
// Licence
// ---------------------------------------------------------------------------
// Ship the project's Apache-2.0 LICENSE inside every artifact, the way the mod
// has always done.
tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
        rename { "LICENSE-${project.name}" }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Expose the (Stonecutter-processed) source directories of this node so that a
// loader branch can compile them straight into its own jar - see
// `dayz-loader.gradle.kts`. This keeps exactly one copy of every class, and one
// copy of every mixin config, on the mod classpath.
val sourceSetsContainer = extensions.getByType<SourceSetContainer>()
val mainSourceSet = sourceSetsContainer.getByName("main")

// Stonecutter's own source-set hook: wires the `stonecutterGenerate` /
// `stonecutterMerge` tasks into the build.
sc.tasks.configureSource(mainSourceSet)

// Where Stonecutter writes this node's version-processed copy of the shared
// sources - `build/generated/stonecutter/<sourceSet>/`.
val generatedRoot = sc.tasks.generatedSourcesDir
val generatedJava = generatedRoot.map { it.dir("${mainSourceSet.name}/java") }
val generatedResources = generatedRoot.map { it.dir("${mainSourceSet.name}/resources") }

// Compile the *processed* tree, never the raw one.
//
// The shared `src/` directories are the single source of truth and contain every
// version's code side by side behind `//? if` markers. Stonecutter only comments
// the inactive branches out in the copy it generates, so handing the raw tree to
// javac would compile every branch at once and could never work. Pointing the
// compile task at the generated tree also means the active node behaves exactly
// like every other node instead of depending on the working tree having been
// "chiselled" to it first.
tasks.named<JavaCompile>("compileJava") {
    dependsOn("stonecutterGenerate")
    setSource(generatedJava)
}

val commonJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
    description = "Stonecutter-processed Java sources of this module"
}

val commonResources: Configuration = configurations.create("commonResources") {
    isCanBeResolved = false
    isCanBeConsumed = true
    description = "Stonecutter-processed resources of this module"
}

// `mainSourceSet.*.sourceDirectories` is only final once Stonecutter has
// finished wiring the node up, which happens after this script body has run, so
// the artifacts are registered from `afterEvaluate`. Registering them eagerly
// yields an empty configuration and the loader branch silently compiles without
// any of the shared sources.
afterEvaluate {
    artifacts.add(commonJava.name, generatedJava.get().asFile)
    artifacts.add(commonResources.name, generatedResources.get().asFile)
}
