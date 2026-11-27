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

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

/**
 * Optional reflection bridge for survival mods that supply thirst and body temperature:
 * <ul>
 *   <li><b>Tough As Nails</b> ({@code toughasnails.api.thirst.ThirstHelper} &amp;
 *       {@code toughasnails.api.temperature.TemperatureHelper})</li>
 *   <li><b>Legendary Survival Overhaul</b> ({@code sfiomn.legendarysurvivaloverhaul.util.CapabilityUtil})</li>
 * </ul>
 * Keeping both behind reflection avoids compile-time and runtime dependencies across
 * all 23 Minecraft versions and 3 mod loaders.
 */
public final class SurvivalModBridge {
    private static final float MAX_THIRST = 20.0F;
    private static final int FLASH_TICKS = 4;

    private static boolean tanInitialized;
    private static boolean tanFailed;
    private static Method tanIsThirstEnabled;
    private static Method tanGetThirst;
    private static Method tanThirstValue;
    private static Method tanHydrationValue;
    private static Method tanIsTempEnabled;
    private static Method tanGetTempData;
    private static Method tanTempLevel;

    private static boolean lsoInitialized;
    private static boolean lsoFailed;
    private static Method lsoGetThirstCap;
    private static Method lsoThirstHydration;
    private static Method lsoThirstSaturation;
    private static Method lsoGetTempCap;
    private static Method lsoTempEnum;

    private SurvivalModBridge() {}

    public record ThirstReading(int level, float fraction, float hydrationFraction) {}

    public record TempReading(float fraction, int color) {}

    public static ThirstReading readThirst(Player player, HudConfig config) {
        if (player == null) {
            return null;
        }
        if (config.toughAsNailsIntegration) {
            ThirstReading tan = readTanThirst(player);
            if (tan != null) {
                return tan;
            }
        }
        if (config.legendarySurvivalIntegration) {
            return readLsoThirst(player);
        }
        return null;
    }

    public static TempReading readTemperature(Player player, HudConfig config, int guiTicks) {
        if (player == null) {
            return null;
        }
        if (config.toughAsNailsIntegration) {
            TempReading tan = readTanTemp(player, guiTicks);
            if (tan != null) {
                return tan;
            }
        }
        if (config.legendarySurvivalIntegration) {
            return readLsoTemp(player, guiTicks);
        }
        return null;
    }

    // --- Tough As Nails ---------------------------------------------------------

    private static void initTan() {
        if (tanInitialized) {
            return;
        }
        tanInitialized = true;
        try {
            Class<?> thirstHelper = Class.forName("toughasnails.api.thirst.ThirstHelper");
            Class<?> iThirst = Class.forName("toughasnails.api.thirst.IThirst");
            tanIsThirstEnabled = thirstHelper.getMethod("isThirstEnabled");
            tanGetThirst = thirstHelper.getMethod("getThirst", Player.class);
            tanThirstValue = iThirst.getMethod("getThirst");
            tanHydrationValue = iThirst.getMethod("getHydration");

            Class<?> tempHelper = Class.forName("toughasnails.api.temperature.TemperatureHelper");
            Class<?> iTemp = Class.forName("toughasnails.api.temperature.ITemperature");
            tanIsTempEnabled = tempHelper.getMethod("isTemperatureEnabled");
            tanGetTempData = tempHelper.getMethod("getTemperatureData", Player.class);
            tanTempLevel = iTemp.getMethod("getLevel");
        } catch (Throwable ignored) {
            tanFailed = true;
        }
    }

    private static ThirstReading readTanThirst(Player player) {
        initTan();
        if (tanFailed) {
            return null;
        }
        try {
            if (!Boolean.TRUE.equals(tanIsThirstEnabled.invoke(null))) {
                return null;
            }
            Object thirst = tanGetThirst.invoke(null, player);
            if (thirst == null) {
                return null;
            }
            int level = ((Number) tanThirstValue.invoke(thirst)).intValue();
            float hydration = ((Number) tanHydrationValue.invoke(thirst)).floatValue();
            float fraction = clamp(level / MAX_THIRST);
            float hydrationFraction = Math.min(fraction, clamp(hydration / MAX_THIRST));
            return new ThirstReading(level, fraction, hydrationFraction);
        } catch (Throwable ignored) {
            tanFailed = true;
            return null;
        }
    }

    private static TempReading readTanTemp(Player player, int guiTicks) {
        initTan();
        if (tanFailed) {
            return null;
        }
        try {
            if (!Boolean.TRUE.equals(tanIsTempEnabled.invoke(null))) {
                return null;
            }
            Object data = tanGetTempData.invoke(null, player);
            if (data == null) {
                return null;
            }
            Object level = tanTempLevel.invoke(data);
            if (level == null) {
                return null;
            }
            return mapTemperatureBand(level.toString(), guiTicks);
        } catch (Throwable ignored) {
            tanFailed = true;
            return null;
        }
    }

    // --- Legendary Survival Overhaul --------------------------------------------

    private static void initLso() {
        if (lsoInitialized) {
            return;
        }
        lsoInitialized = true;
        try {
            Class<?> capUtil = Class.forName("sfiomn.legendarysurvivaloverhaul.util.CapabilityUtil");
            Class<?> iThirst = Class.forName("sfiomn.legendarysurvivaloverhaul.api.thirst.IThirstCapability");
            lsoGetThirstCap = capUtil.getMethod("getThirstCapability", Player.class);
            lsoThirstHydration = iThirst.getMethod("getHydrationLevel");
            lsoThirstSaturation = iThirst.getMethod("getSaturationLevel");

            Class<?> iTemp = Class.forName("sfiomn.legendarysurvivaloverhaul.api.temperature.ITemperatureCapability");
            lsoGetTempCap = capUtil.getMethod("getTempCapability", Player.class);
            lsoTempEnum = iTemp.getMethod("getTemperatureEnum");
        } catch (Throwable ignored) {
            lsoFailed = true;
        }
    }

    private static ThirstReading readLsoThirst(Player player) {
        initLso();
        if (lsoFailed) {
            return null;
        }
        try {
            Object cap = lsoGetThirstCap.invoke(null, player);
            if (cap == null) {
                return null;
            }
            int level = ((Number) lsoThirstHydration.invoke(cap)).intValue();
            float sat = ((Number) lsoThirstSaturation.invoke(cap)).floatValue();
            float fraction = clamp(level / MAX_THIRST);
            float hydrationFraction = Math.min(fraction, clamp(sat / MAX_THIRST));
            return new ThirstReading(level, fraction, hydrationFraction);
        } catch (Throwable ignored) {
            lsoFailed = true;
            return null;
        }
    }

    private static TempReading readLsoTemp(Player player, int guiTicks) {
        initLso();
        if (lsoFailed) {
            return null;
        }
        try {
            Object cap = lsoGetTempCap.invoke(null, player);
            if (cap == null) {
                return null;
            }
            Object tempEnum = lsoTempEnum.invoke(cap);
            if (tempEnum == null) {
                return null;
            }
            return mapTemperatureBand(tempEnum.toString(), guiTicks);
        } catch (Throwable ignored) {
            lsoFailed = true;
            return null;
        }
    }

    // --- Shared Band Mapping ----------------------------------------------------

    private static TempReading mapTemperatureBand(String bandName, int guiTicks) {
        boolean lit = ((guiTicks / FLASH_TICKS) & 1) == 0;
        return switch (bandName) {
            case "ICY", "FROSTBITE" -> new TempReading(0.10F,
                    lit ? HudTheme.TEMP_ICY : HudTheme.TEMP_ICY_DIM);
            case "COLD" -> new TempReading(0.30F, HudTheme.TEMP_COLD);
            case "WARM" -> new TempReading(0.75F, HudTheme.TEMP_WARM);
            case "HOT", "HEAT_STROKE" -> new TempReading(1.00F,
                    lit ? HudTheme.TIER_CRITICAL : HudTheme.TIER_CRITICAL_DIM);
            default -> new TempReading(0.50F, HudTheme.TIER_NORMAL);
        };
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
