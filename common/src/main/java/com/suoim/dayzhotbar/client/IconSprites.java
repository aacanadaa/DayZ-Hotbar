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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * The status icons, borrowed from vanilla's own {@code textures/gui/icons.png}.
 * <p>
 * Reusing Minecraft's sprites means the HUD cannot clash with a resource pack
 * and needs no assets of its own. The icons are drawn far larger than vanilla
 * draws them (2x), and cropped to a fraction of their height, which is what
 * turns a discrete "full / half / empty" icon into a continuous DayZ-style fill.
 * <p>
 * Every coordinate below was read off the real 256x256 sheet, not from memory:
 * the hearts occupy column 16 of row 0, the armour row is at v=9, the air
 * bubbles at v=18 and the food shanks at v=27. Within a row the empty variant
 * sits at u=16, the half at u=25 and the full at u=34 or u=52 depending on the
 * row - which is why each one is named rather than computed.
 */
public final class IconSprites {
    private IconSprites() {}

    /** The vanilla sprite sheet. 256x256, so the 6-arg blit overload is exact. */
    public static final ResourceLocation ICONS = new ResourceLocation("textures/gui/icons.png");

    /** Source tile size on the sheet. */
    public static final int TILE = 9;
    /** Sheet dimensions. */
    public static final int SHEET = 256;

    // --- Row origins ------------------------------------------------------------
    public static final int V_HEART = 0;
    public static final int V_ARMOR = 9;
    public static final int V_AIR = 18;
    public static final int V_FOOD = 27;

    // --- Heart columns (row 0) --------------------------------------------------
    public static final int U_HEART_EMPTY = 16;
    public static final int U_HEART_FULL = 52;
    public static final int U_HEART_HALF = 61;
    public static final int U_ABSORB_FULL = 160;
    public static final int U_ABSORB_HALF = 169;

    // --- Armour columns (row 9) -------------------------------------------------
    public static final int U_ARMOR_EMPTY = 16;
    public static final int U_ARMOR_HALF = 25;
    public static final int U_ARMOR_FULL = 34;

    // --- Air columns (row 18) ---------------------------------------------------
    // Both are hollow outlines; the darker one reads as spent, the lighter as held.
    public static final int U_AIR_EMPTY = 16;
    public static final int U_AIR_FULL = 34;

    // --- Food columns (row 27) --------------------------------------------------
    public static final int U_FOOD_EMPTY = 16;
    public static final int U_FOOD_FULL = 52;
    public static final int U_FOOD_HALF = 61;

    /**
     * Blits a 9x9 source tile scaled to {@code size} on screen.
     * <p>
     * Uses the 11-argument overload so the destination size and the source size
     * are independent - no pose stack juggling needed to scale an icon.
     */
    public static void sprite(GuiGraphics graphics, int x, int y, int size, int u, int v) {
        graphics.blit(ICONS, x, y, size, size, (float) u, (float) v, TILE, TILE, SHEET, SHEET);
    }

    /**
     * Draws an icon that is {@code fraction} full, filling from the bottom up.
     * <p>
     * The empty variant is drawn first as the container, then the full variant is
     * blitted over it with the scissor box clipped to the filled height. There is
     * no {@code blitSprite} in 1.20.1, so scissoring is how a partial fill is done.
     *
     * @param fraction 0.0 (empty) to 1.0 (full); values outside are clamped
     */
    public static void fill(GuiGraphics graphics, int x, int y, int size,
                            int uEmpty, int vEmpty, int uFull, int vFull, float fraction) {
        sprite(graphics, x, y, size, uEmpty, vEmpty);

        if (fraction <= 0.0F) {
            return;
        }
        if (fraction >= 1.0F) {
            sprite(graphics, x, y, size, uFull, vFull);
            return;
        }

        int filled = Math.max(1, Math.round(size * fraction));
        graphics.enableScissor(x, y + size - filled, x + size, y + size);
        sprite(graphics, x, y, size, uFull, vFull);
        graphics.disableScissor();
    }

    /**
     * A translucent bar overlaid on the bottom {@code fraction} of an icon, used to
     * show food saturation on top of the food level.
     */
    public static void overlay(GuiGraphics graphics, int x, int y, int size, float fraction, int color) {
        if (fraction <= 0.0F) {
            return;
        }
        int filled = Math.max(1, Math.round(size * Math.min(1.0F, fraction)));
        graphics.fill(x + 1, y + size - filled, x + size - 1, y + size - 1, color);
    }
}
