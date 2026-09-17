/*
 * DayZ Hotbar - Gradle settings
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Multi-version / multi-loader project powered by Stonecutter.
 *
 * The build is structured as a Stonecutter *tree* with one *branch* per source
 * module. Each branch owns its own shared `src/` directory and gets one node
 * (a real Gradle subproject) per Minecraft version it supports:
 *
 *     :common:1.21.1     ->  common/versions/1.21.1 , sources from common/src
 *     :fabric:1.21.1     ->  fabric/versions/1.21.1 , sources from fabric/src
 *     :neoforge:26.2     ->  neoforge/versions/26.2 , sources from neoforge/src
 *
 * Version-independent dependency coordinates live in `versions/<mc>/gradle.properties`.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }

    // Plugin versions are pinned here so the branch build scripts stay free of
    // version literals and every module always agrees on a toolchain.
    //
    // The two Loom ids are listed even though no build script asks for them by
    // name: `dev.kikugie.loom-back-compat` applies one of them programmatically
    // depending on whether the node's Minecraft release is obfuscated, and a
    // programmatic `pluginManager.apply(id)` can only be resolved this way.
    val loomVersion = providers.gradleProperty("loomx.loom_version").getOrElse("1.18.1")
    plugins {
        id("net.fabricmc.fabric-loom") version loomVersion
        id("net.fabricmc.fabric-loom-remap") version loomVersion
        id("dev.kikugie.loom-back-compat") version "0.4.2"
        id("net.neoforged.moddev") version "2.0.147"
        id("net.neoforged.moddev.legacyforge") version "2.0.147"
        // ForgeGradle 7 is `net.minecraftforge:forgegradle`, published on the
        // Gradle Plugin Portal as well as Forge's maven.
        id("net.minecraftforge.gradle") version "7.0.40"
        id("me.modmuss50.mod-publish-plugin") version "2.1.1"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Applied at the settings level so it can decide which flavour of Fabric
    // Loom a node needs before the branch build script runs.
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

// Stonecutter node projects are not ordinary subprojects: their buildscript
// classpath is assembled by the controller, and `loom-back-compat` contributes
// the Loom flavour it selected as a *buildscript dependency*. Those are resolved
// against the project's own buildscript repositories, which would otherwise be
// empty, so populate them before any project is configured.
gradle.beforeProject {
    buildscript.repositories.apply {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

// ---------------------------------------------------------------------------
// Supported version matrix
// ---------------------------------------------------------------------------
// Every entry below is a Minecraft version the project is *declared* to target.
// A branch only receives the versions its loader actually exists for, which is
// why the lists differ:
//
//   * Fabric    - every version.
//   * NeoForge  - 1.20.6 and newer. 1.20.1 predates NeoForge entirely, 1.20.2
//                 still uses the SimpleChannel stack, 1.20.3 has no NeoForge
//                 release at all, and 1.20.4's registrar API predates
//                 `StreamCodec` so it would need a payload type of its own.
//   * Forge     - see "Not enabled yet" below.
//
// Adding a version is a one-line change here plus a `versions/<mc>/gradle.properties`
// file - see docs/BUILDING.md ("Adding a Minecraft version").

val fabricVersions = listOf("1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
// 1.20.5 is Fabric-only: NeoForge published that release without a
// `moddev-config.json` (only an installer), which ModDevGradle needs, and Forge
// has no 1.20.5 release at all.
val neoforgeVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
// Forge stopped being a first-class target after 1.20.x and the ecosystem moved
// to NeoForge. Unlike the sibling inventory mod, this module's HUD *is* a Forge
// layer, so a version only qualifies when Forge actually ships its layer API
// (`ForgeLayeredDraw` + `AddGuiOverlayLayersEvent`). Verified by inspecting each
// Forge universal jar:
//
//   * absent on 1.21 (51.0.x), 1.21.6 (56.0.x) and 1.21.7 (57.0.x) - Forge was
//     mid-rewrite and shipped no HUD hook at all on those lines;
//   * 1.21.2 is absent because Forge never published that release;
//   * 1.20.1 is absent because Forge runs on SRG names there, needing
//     reobfuscation and a Searge mixin refmap that no Gradle-9-capable plugin
//     provides. 1.20.1 still ships for Fabric, where it needs none of that.
//
// 1.21.8 moved to EventBus 7, which split `net.minecraftforge.eventbus.api` into
// `bus` and `listener` subpackages; the import block in ForgeHudHandler switches
// on `//? if >=1.21.8`. The layer tree also changed shape on that line - see the
// handler's class comment.
val forgeVersions = listOf("1.20.6", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
val commonVersions = (fabricVersions + neoforgeVersions + forgeVersions).distinct()

// ---------------------------------------------------------------------------
// Not enabled yet
// ---------------------------------------------------------------------------
// The only target combinations the matrix above does not cover. See
// docs/BUILDING.en.md section 7 for the detail.
//
//   * Forge 1.20.1 - the toolchain problem above.
//   * Forge 1.21, 1.21.6, 1.21.7 - those Forge lines ship no HUD layer API, so
//     there is nothing for this mod to hook. NeoForge covers 1.21.6 and 1.21.7,
//     and Fabric covers 1.21.
//   * Forge 1.21.2 - Forge never published it.
//   * 26.x (Forge) - Forge has no buildable 26.x line here; NeoForge is the
//     supported route.
//
// Fabric covers every version from 1.20.1 up, and NeoForge every version from
// 1.20.6 up.

stonecutter {
    create(rootProject) {
        // Only named branches carry nodes here. The root branch is left empty on
        // purpose: it would otherwise create a project per version with no build
        // script and no artifacts, which the controller cannot order publishing
        // tasks for.
        //
        // Per-version dependency coordinates still live in
        // `versions/<mc>/gradle.properties`; the `prop` helpers in build-logic
        // read those files directly.
        branch("common") { versions(*commonVersions.toTypedArray()) }
        branch("fabric") { versions(*fabricVersions.toTypedArray()) }
        branch("forge") { versions(*forgeVersions.toTypedArray()) }
        branch("neoforge") { versions(*neoforgeVersions.toTypedArray()) }
    }
}

rootProject.name = "dayz-hotbar"
