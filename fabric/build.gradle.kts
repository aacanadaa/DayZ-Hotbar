/*
 * DayZ Hotbar - Fabric module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version. `:common:<mc>`'s processed
 * sources are compiled straight into this project by `dayz-loader`, so this jar
 * is self-contained: one copy of every class and of the mixin config.
 */

plugins {
    id("dayz-loader")
    id("dev.kikugie.loom-back-compat")
}

// Keep every source under `src/main/java`. `dayz-loader` applies `dayz-common`
// *during plugin application*, which wires Stonecutter's source processing and
// then points `compileJava` at the generated tree only. A source root added in
// this script body would never be preprocessed and never compiled. Client-only
// safety comes from the `"environment": "client"` marker on the entrypoint, not
// from a separate source root.

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    loomx.applyMojangMappings()

    // Fabric Loader only. The HUD hooks vanilla's own `Gui` through Mixin, so it
    // needs no Fabric API and declares none - publishing must not require it
    // either, or users would be told to install something they do not need.
    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
}

if (sc.current.parsed < "26") {
    loom {
        mixin {
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("dayz-hotbar.refmap.json")
        }
    }
}

// Run configurations are only meaningful for the node the working tree is
// currently chiselled to; Gradle cannot generate IDE run configs for several
// versions of the same project at once.
if (isActiveNode) {
    loom {
        runs {
            named("client") {
                client()
                ideConfigGenerated(true)
                runDir("../../run")
                configName = "Fabric Client ($mc)"
            }
            named("server") {
                server()
                ideConfigGenerated(true)
                runDir("../../run")
                configName = "Fabric Server ($mc)"
            }
        }
    }
}

configurations {
    named("runtimeClasspath") { exclude(group = "net.fabricmc", module = "sponge-mixin") }
}
