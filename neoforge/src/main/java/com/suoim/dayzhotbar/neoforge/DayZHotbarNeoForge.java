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
package com.suoim.dayzhotbar.neoforge;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HUD itself needs no initialisation - {@link NeoForgeHudHandler} hooks
 * NeoForge's HUD layer renderer and everything the mod draws comes out of the
 * common module.
 * <p>
 * This class exists because NeoForge requires a mod entrypoint, and because the log
 * line is what a support request can be answered from: it says which build is
 * actually loaded. There is deliberately no client-only state here, so the class is
 * safe to load on a dedicated server - the handler is annotated {@code Dist.CLIENT}
 * and is never constructed without a client.
 */
@Mod(DayZHotbarNeoForge.MOD_ID)
public class DayZHotbarNeoForge {
    public static final String MOD_ID = "dayz_hotbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public DayZHotbarNeoForge() {
        LOGGER.info("DayZ Hotbar loaded.");
    }
}
