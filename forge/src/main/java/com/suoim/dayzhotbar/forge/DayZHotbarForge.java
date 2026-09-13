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
package com.suoim.dayzhotbar.forge;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HUD itself needs no initialisation - Forge applies {@code GuiMixin} from
 * {@code dayz-hotbar.client.mixins.json} before this runs, and everything the mod
 * draws comes out of the common module.
 * <p>
 * This class exists because Forge requires a mod entrypoint, and because the log
 * line is what a support request can be answered from: it says which build is
 * actually loaded. There is deliberately no client-only state here, so the class
 * is safe to load on a dedicated server - the mixin config lists {@code GuiMixin}
 * under {@code client}, so nothing touches a client class where there is no
 * client.
 */
@Mod(DayZHotbarForge.MOD_ID)
public class DayZHotbarForge {
    public static final String MOD_ID = "dayz_hotbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public DayZHotbarForge() {
        LOGGER.info("DayZ Hotbar loaded.");
    }
}
