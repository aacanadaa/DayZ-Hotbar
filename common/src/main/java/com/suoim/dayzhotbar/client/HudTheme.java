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
 * Every value here mirrors a constant that DayZ Inventory uses, so the two mods
 * read as one interface. Change one and the other should follow.
 */
public final class HudTheme {
    private HudTheme() {}

    // --- Panels -----------------------------------------------------------------
    /** Flat panel fill. Translucent so the world shows through, as in DayZ. */
    public static final int PANEL_BG = 0x9E0C0C0C;
    /** Inner wash for a small slot. */
    public static final int SLOT_INNER = 0x18FFFFFF;
    /** Inner wash for a large slot. */
    public static final int SLOT_INNER_LARGE = 0x15FFFFFF;

    // --- Lines and highlights ---------------------------------------------------
    /** 1px separator between stacked elements. */
    public static final int HAIRLINE = 0x10FFFFFF;
    /** Hover / focus wash. */
    public static final int HOVER = 0x30FFFFFF;

    // --- Text -------------------------------------------------------------------
    public static final int TEXT = 0xFFDFDFDF;
    public static final int TEXT_BRIGHT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFF888888;

    // --- Accents ----------------------------------------------------------------
    /** The red used for destructive / unavailable affordances. */
    public static final int ACCENT_RED = 0xFFE04040;

    // --- Hotbar slot selection states (see SlotState) ----------------------------
    /** Empty slot: present but inert. */
    public static final int STATE_EMPTY = 0xFF4A4A4A;
    /** Slot holds an item but is not selected. */
    public static final int STATE_FILLED = 0xFF9A9A9A;
    /** The active (held) slot. */
    public static final int STATE_ACTIVE = 0xFF5FBF4F;
    /** The active slot, but the item cannot be used right now. */
    public static final int STATE_BLOCKED = 0xFFE04040;

    /** Translucent version of a state colour, for the active slot's inner glow. */
    public static int wash(int argb, int alpha) {
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    /** The DayZ Inventory panel: a single flat fill, no border. */
    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL_BG);
    }

    /** A 1px outline drawn inside the given rectangle, in the given colour. */
    public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);                       // top
        graphics.fill(x, y + height - 1, x + width, y + height, color);     // bottom
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);              // left
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color); // right
    }

    /**
     * A downward chevron, the DayZ-style "this value is moving" marker.
     * <p>
     * Drawn as five 1px cells rather than a texture, matching the sibling mod's
     * textureless approach. Width 5, height 3.
     */
    public static void chevronDown(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x, y, x + 1, y + 1, color);
        graphics.fill(x + 4, y, x + 5, y + 1, color);
        graphics.fill(x + 1, y + 1, x + 2, y + 2, color);
        graphics.fill(x + 3, y + 1, x + 4, y + 2, color);
        graphics.fill(x + 2, y + 2, x + 3, y + 3, color);
    }

    /** An upward chevron. Width 5, height 3. See {@link #chevronDown}. */
    public static void chevronUp(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 2, y, x + 3, y + 1, color);
        graphics.fill(x + 1, y + 1, x + 2, y + 2, color);
        graphics.fill(x + 3, y + 1, x + 4, y + 2, color);
        graphics.fill(x, y + 2, x + 1, y + 3, color);
        graphics.fill(x + 4, y + 2, x + 5, y + 3, color);
    }

    /** Draws {@code count} stacked chevrons in the given direction. */
    public static void chevrons(GuiGraphics graphics, int centerX, int topY, int count, boolean up, int color) {
        for (int i = 0; i < count; i++) {
            int y = topY + i * 4;
            if (up) {
                chevronUp(graphics, centerX - 2, y, color);
            } else {
                chevronDown(graphics, centerX - 2, y, color);
            }
        }
    }
}
