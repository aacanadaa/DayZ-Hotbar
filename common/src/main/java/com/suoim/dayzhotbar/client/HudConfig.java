/*
 * DayZ Hotbar
 * Copyright 2026 suoim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.suoim.dayzhotbar.client;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lightweight client configuration backed by {@code config/dayz_hotbar.properties}.
 * <p>
 * Uses standard {@link Properties} so it works identically across Fabric, Forge and
 * NeoForge from 1.20.1 through 26.3 without an external config library.
 */
public final class HudConfig {
    private static final String FILE_NAME = "dayz_hotbar.properties";

    private static HudConfig instance;

    public boolean enableHotbar = true;
    public boolean enablePlayerPanel = true;
    public boolean enableStatusRow = true;
    public boolean showJumpBar = true;
    public boolean showEffects = true;
    public boolean showHealth = true;
    public boolean showFood = true;
    public boolean showWater = true;
    public boolean showTemperature = true;
    public boolean showAir = true;
    public boolean showAbsorption = true;
    public boolean showXp = true;
    public boolean toughAsNailsIntegration = true;
    public boolean legendarySurvivalIntegration = true;

    private HudConfig() {}

    public static HudConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static HudConfig load() {
        HudConfig cfg = new HudConfig();
        Path path = configPath();
        if (path == null) {
            return cfg;
        }

        Properties props = new Properties();
        boolean exists = Files.isRegularFile(path);
        if (exists) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            } catch (IOException ignored) {
                return cfg;
            }
        }

        cfg.enableHotbar = parseBool(props, "enableHotbar", true);
        cfg.enablePlayerPanel = parseBool(props, "enablePlayerPanel", true);
        cfg.enableStatusRow = parseBool(props, "enableStatusRow", true);
        cfg.showJumpBar = parseBool(props, "showJumpBar", true);
        cfg.showEffects = parseBool(props, "showEffects", true);
        cfg.showHealth = parseBool(props, "showHealth", true);
        cfg.showFood = parseBool(props, "showFood", true);
        cfg.showWater = parseBool(props, "showWater", true);
        cfg.showTemperature = parseBool(props, "showTemperature", true);
        cfg.showAir = parseBool(props, "showAir", true);
        cfg.showAbsorption = parseBool(props, "showAbsorption", true);
        cfg.showXp = parseBool(props, "showXp", true);
        cfg.toughAsNailsIntegration = parseBool(props, "toughAsNailsIntegration", true);
        cfg.legendarySurvivalIntegration = parseBool(props, "legendarySurvivalIntegration", true);

        if (!exists || props.size() < 14) {
            save(cfg, path);
        }
        return cfg;
    }

    public boolean isStatEnabled(Stat stat) {
        return switch (stat) {
            case HEALTH -> showHealth;
            case FOOD -> showFood;
            case WATER -> showWater;
            case TEMPERATURE -> showTemperature;
            case AIR -> showAir;
            case ABSORPTION -> showAbsorption;
            case XP -> showXp;
        };
    }

    private static boolean parseBool(Properties props, String key, boolean fallback) {
        String raw = props.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        return Boolean.parseBoolean(raw.trim());
    }

    private static Path configPath() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameDirectory == null) {
            return null;
        }
        return mc.gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static void save(HudConfig cfg, Path path) {
        Properties props = new Properties();
        props.setProperty("enableHotbar", Boolean.toString(cfg.enableHotbar));
        props.setProperty("enablePlayerPanel", Boolean.toString(cfg.enablePlayerPanel));
        props.setProperty("enableStatusRow", Boolean.toString(cfg.enableStatusRow));
        props.setProperty("showJumpBar", Boolean.toString(cfg.showJumpBar));
        props.setProperty("showEffects", Boolean.toString(cfg.showEffects));
        props.setProperty("showHealth", Boolean.toString(cfg.showHealth));
        props.setProperty("showFood", Boolean.toString(cfg.showFood));
        props.setProperty("showWater", Boolean.toString(cfg.showWater));
        props.setProperty("showTemperature", Boolean.toString(cfg.showTemperature));
        props.setProperty("showAir", Boolean.toString(cfg.showAir));
        props.setProperty("showAbsorption", Boolean.toString(cfg.showAbsorption));
        props.setProperty("showXp", Boolean.toString(cfg.showXp));
        props.setProperty("toughAsNailsIntegration", Boolean.toString(cfg.toughAsNailsIntegration));
        props.setProperty("legendarySurvivalIntegration", Boolean.toString(cfg.legendarySurvivalIntegration));

        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                props.store(out, "DayZ Hotbar client configuration");
            }
        } catch (IOException ignored) {
        }
    }
}
