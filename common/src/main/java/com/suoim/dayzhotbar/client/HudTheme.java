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
 * The visual language shared with the DayZ Inventory mod: flat translucent
 * near-black panels, hairline separators, desaturated grey text, and no borders
 * or chrome of any kind.
 * <p>
 * Every panel and slot value here is lifted from a constant DayZ Inventory
 * actually uses. In particular its slots are an 18px pitch with a 16x16 inner
 * wash and <em>no outline</em> - slot state is carried by the wash colour, which
 * is why nothing here draws a border.
 */
public final class HudTheme {
    private HudTheme() {}

    // --- Panels -----------------------------------------------------------------
    /** Flat panel fill. Translucent so the world shows through, as in DayZ. */
    public static final int PANEL_BG = 0x9E0C0C0C;
    /** The standard inner wash for a slot. DayZ Inventory's own slot value. */
    public static final int SLOT_INNER = 0x18FFFFFF;
    /** A slot that holds something. A subtle lift over {@link #SLOT_INNER}. */
    public static final int SLOT_FILLED = 0x22FFFFFF;

    /** 1px separator between stacked elements. */
    public static final int HAIRLINE = 0x10FFFFFF;

    // --- Text -------------------------------------------------------------------
    public static final int TEXT_BRIGHT = 0xFFFFFFFF;

    // --- Status tiers -----------------------------------------------------------
    /** Healthy. */
    public static final int TIER_NORMAL = 0xFFEDEDED;
    /** Below half. */
    public static final int TIER_YELLOW = 0xFFE8C33A;
    /** Low. */
    public static final int TIER_RED = 0xFFE04040;
    /** Critical - this tier flashes. */
    public static final int TIER_CRITICAL = 0xFFFF3B30;
    /** The dark half of the critical flash. */
    public static final int TIER_CRITICAL_DIM = 0x66FF3B30;
    /**
     * The un-filled remainder of a status icon. Kept faint: at a brighter value the
     * empty part of the shape looks solid and the fill level stops reading.
     */
    public static final int ICON_EMPTY = 0x2AFFFFFF;
    /** Absorption is always its own colour - it is a bonus, not a warning. */
    public static final int TIER_ABSORPTION = 0xFFF2C94C;

    // --- Hotbar slot states -----------------------------------------------------
    /** The slot in hand, settled. */
    public static final int STATE_ACTIVE = 0xFF5FBF4F;
    /** The slot in hand, mid-swap. Resolves to {@link #STATE_ACTIVE}. */
    public static final int STATE_SWAPPING = 0xFFE8C33A;
    /** The slot in hand, but the item cannot be used right now. */
    public static final int STATE_BLOCKED = 0xFFE04040;

    /** Translucent version of a colour, for slot washes. */
    public static int wash(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    /** Linear blend between two ARGB colours. {@code t} is clamped to 0..1. */
    public static int lerp(int from, int to, float t) {
        float f = Math.max(0.0F, Math.min(1.0F, t));
        int a = lerpChannel(from >>> 24, to >>> 24, f);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, f);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, f);
        int b = lerpChannel(from & 0xFF, to & 0xFF, f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }

    /** The DayZ Inventory panel: a single flat fill, no border. */
    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL_BG);
    }

    // --- Rank-insignia trend chevron --------------------------------------------
    /**
     * The trend marker, drawn as a bold stacked chevron in the shape of a US Army
     * rank insignia rather than a thin arrow. Each source cell is drawn at the same
     * 2px scale the status icons use, so the marker is 18x10 with four-pixel arms -
     * a thin 1px arrow disappears next to icons this size.
     * <p>
     * Always white. Direction is carried by the orientation, not the colour, so the
     * marker never competes with the status tier colours above it.
     */
    private static final String[] CHEVRON = {
        "....#....",
        "...###...",
        "..#####..",
        ".##...##.",
        "##.....##"
    };

    /** Size of one chevron source cell. Matches the status icons' scale. */
    private static final int CHEVRON_PIXEL = 2;

    /** Height of one chevron. */
    public static final int CHEVRON_H = CHEVRON.length * CHEVRON_PIXEL;
    /** Width of the marker. */
    public static final int CHEVRON_W = CHEVRON[0].length() * CHEVRON_PIXEL;
    /** Vertical pitch two stacked chevrons sit at, leaving a 1px gap. */
    public static final int CHEVRON_PITCH = CHEVRON_H + 1;
    /** Vertical space two stacked chevrons need. */
    public static final int CHEVRON_STACK_H = CHEVRON_H * 2 + 1;

    /**
     * Draws {@code count} stacked rank chevrons, white, pointing up when
     * {@code up}. The alpha is baked into the colour so the marker fades out
     * rather than snapping off.
     */
    public static void chevrons(GuiGraphics graphics, int centerX, int topY, int count,
                                boolean up, float alpha) {
        int a = Math.round(255.0F * Math.max(0.0F, Math.min(1.0F, alpha)));
        int color = (a << 24) | 0x00FFFFFF;
        int left = centerX - CHEVRON_W / 2;

        for (int i = 0; i < count; i++) {
            int top = topY + i * CHEVRON_PITCH;
            for (int row = 0; row < CHEVRON.length; row++) {
                String line = CHEVRON[up ? row : CHEVRON.length - 1 - row];
                for (int col = 0; col < line.length(); col++) {
                    if (line.charAt(col) == '#') {
                        int px = left + col * CHEVRON_PIXEL;
                        int py = top + row * CHEVRON_PIXEL;
                        graphics.fill(px, py, px + CHEVRON_PIXEL, py + CHEVRON_PIXEL, color);
                    }
                }
            }
        }
    }
}
