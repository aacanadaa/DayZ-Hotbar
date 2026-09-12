/*
 * DayZ Hotbar
 * Copyright 2026 suoim
 *
 * Licensed under the PolyForm Noncommercial License 1.0.0. You may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *     https://polyformproject.org/licenses/noncommercial/1.0.0
 *
 * Required Notice: Copyright 2026 suoim
 */
package com.suoim.dayzhotbar.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HUD itself needs no initialisation - Fabric Loader applies
 * {@code GuiMixin} from {@code dayz-hotbar.client.mixins.json} before this runs.
 * <p>
 * This entrypoint exists as the hook for anything that does need registering
 * later, and to leave a line in the log so a support request can confirm which
 * build is actually loaded.
 */
public class DayZHotbarFabricClient implements ClientModInitializer {
    public static final String MOD_ID = "dayz_hotbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("DayZ Hotbar loaded.");
    }
}
