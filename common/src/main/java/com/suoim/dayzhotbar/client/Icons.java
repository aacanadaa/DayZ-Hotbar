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

/**
 * The status icons, drawn as pixel art rather than borrowed from a sheet.
 * <p>
 * Each icon is a 9x9 character grid; every {@code #} becomes one square of the
 * requested pixel size. At the size the HUD uses that is a 2px square, giving an
 * 18x18 icon whose pixels match the scale of everything else in the mod.
 * <p>
 * Drawing them here rather than shipping a PNG keeps the mod assetless, but the
 * real reason is that these need to be recoloured by status tier and clipped to a
 * fill fraction. A bitmap would have to be redrawn for every colour; a shape can
 * simply be drawn twice.
 */
public final class Icons {
    private Icons() {}

    /** Source grid dimension. Every shape below is exactly this wide and tall. */
    public static final int GRID = 9;

    /**
     * A heart. The notch between the lobes is three cells wide on purpose - at one
     * or two it is invisible once the shape is drawn solid, and the whole thing
     * reads as a blob.
     */
    public static final String[] HEART = {
        ".........",
        ".##...##.",
        "#########",
        "#########",
        "#########",
        ".#######.",
        "..#####..",
        "...###...",
        "....#...."
    };

    /** An apple: it narrows to a stem, which is what stops it reading as an egg. */
    public static final String[] APPLE = {
        "....#....",
        "...###...",
        "..#####..",
        ".#######.",
        "#########",
        "#########",
        "#########",
        ".#######.",
        "..#####.."
    };

    /** A chestplate: wide shoulders, a deep neck notch, tapering to the waist. */
    public static final String[] ARMOR = {
        ".........",
        ".##...##.",
        "#########",
        "#########",
        "#########",
        ".#######.",
        "..#####..",
        "..#####..",
        "...###..."
    };

    /**
     * A medical cross, used for health. Drawn in the tier colour rather than being
     * permanently red, so it still goes yellow and flashes as health drops - a cross
     * that was always red would say nothing about how much health is left.
     */
    public static final String[] CROSS = {
        "...###...",
        "...###...",
        "...###...",
        "#########",
        "#########",
        "#########",
        "...###...",
        "...###...",
        "...###..."
    };

    /** A bubble. */
    public static final String[] BUBBLE = {
        "...###...",
        ".#######.",
        ".#######.",
        "#########",
        "#########",
        "#########",
        ".#######.",
        ".#######.",
        "...###..."
    };

    /**
     * Draws one shape in a single flat colour.
     *
     * @param pixel size of one source cell on screen
     */
    public static void draw(GuiGraphics graphics, String[] shape, int x, int y, int pixel, int color) {
        for (int row = 0; row < shape.length; row++) {
            String line = shape[row];
            for (int col = 0; col < line.length(); col++) {
                if (line.charAt(col) == '#') {
                    int px = x + col * pixel;
                    int py = y + row * pixel;
                    graphics.fill(px, py, px + pixel, py + pixel, color);
                }
            }
        }
    }

    /**
     * Draws a shape filled to {@code fraction} from the bottom up: the whole shape
     * first as a dim silhouette, then again in the fill colour with the scissor box
     * clipped to the filled height.
     * <p>
     * 1.20.1 has no {@code blitSprite}, and there is nothing to blit here anyway -
     * scissoring is simply how a partial fill of any drawn shape is done.
     *
     * @param fraction 0.0 (empty) to 1.0 (full); values outside are clamped
     */
    public static void drawFilled(GuiGraphics graphics, String[] shape, int x, int y, int pixel,
                                  float fraction, int fillColor) {
        draw(graphics, shape, x, y, pixel, HudTheme.ICON_EMPTY);

        if (fraction <= 0.0F) {
            return;
        }

        int size = GRID * pixel;
        if (fraction >= 1.0F) {
            draw(graphics, shape, x, y, pixel, fillColor);
            return;
        }

        int filled = Math.max(1, Math.round(size * fraction));
        graphics.enableScissor(x, y + size - filled, x + size, y + size);
        draw(graphics, shape, x, y, pixel, fillColor);
        graphics.disableScissor();
    }

    /**
     * A translucent wash over the bottom {@code fraction} of a shape's bounding
     * box, used to show food saturation on top of the food level.
     */
    public static void overlayBottom(GuiGraphics graphics, int x, int y, int size, int pixel,
                                     float fraction, int color) {
        if (fraction <= 0.0F) {
            return;
        }
        int filled = Math.max(1, Math.round(size * Math.min(1.0F, fraction)));
        graphics.fill(x + pixel, y + size - filled, x + size - pixel, y + size - pixel, color);
    }
}
