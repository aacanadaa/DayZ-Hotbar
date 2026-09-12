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

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * The bottom-right status readout: a horizontal row of icons on a shared panel,
 * each filled to its current level and coloured by how much is left, each with a
 * rank-chevron trend marker.
 * <p>
 * The marker sits <em>above</em> its icon rather than below. That is not an
 * arbitrary choice: an 18px icon plus a 21px marker stack is 41px, which is taller
 * than the hotbar's own band, so with the marker underneath the icons were forced
 * up off the bottom of the screen. Above, the icons can sit exactly on the hotbar's
 * baseline where they belong.
 * <p>
 * The row is right-aligned, so stats that come and go (armour you are not wearing,
 * air while you are on land) do not shift the ones that are always there - the row
 * simply grows and shrinks from the left.
 */
public final class StatusRow {
    private StatusRow() {}

    /** Size of one source pixel of an icon. */
    private static final int PIXEL = 2;
    /** Icon edge length. A 9x9 shape at 2px per cell. */
    public static final int ICON = Icons.GRID * PIXEL;
    /** Gap between adjacent icons. */
    private static final int GAP = 6;
    /** Space reserved above the icons for the trend marker and the level number. */
    public static final int ARROW_H = HudTheme.CHEVRON_STACK_H + 1;
    /** Total cell height. Note there is no headroom below - the panel bottom is the row bottom. */
    public static final int CELL_H = ARROW_H + ICON;
    /** Padding between the cell contents and the panel edge. Kept tight, as on the hotbar. */
    private static final int PAD = 2;
    /**
     * Distance from the right and bottom screen edges. Matches the hotbar's own
     * margin, so the two rows share a baseline instead of merely looking close.
     */
    private static final int MARGIN = 4;

    /**
     * One rendered stat: how full it is, and what its trend marker should say.
     *
     * @param fraction   0..1 fill amount
     * @param saturation 0..1 secondary fill, only used by food
     * @param chevrons   0, 1 or 2
     * @param up         trend direction; only meaningful when chevrons &gt; 0
     * @param alpha      marker opacity, so it fades rather than snapping off
     * @param level      experience level, only used by XP
     */
    public record Sample(Stat stat, float fraction, float saturation,
                         int chevrons, boolean up, float alpha, int level) {}

    public static void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight,
                              List<Sample> samples, int guiTicks) {
        if (samples.isEmpty()) {
            return;
        }

        int count = samples.size();
        int rowWidth = count * ICON + (count - 1) * GAP;
        int rowX = screenWidth - MARGIN - rowWidth;
        int rowY = screenHeight - MARGIN - CELL_H;

        // One flat panel behind the whole readout, the way every DayZ Inventory
        // element sits on a section panel rather than floating over the world.
        HudTheme.panel(graphics, rowX - PAD, rowY - PAD, rowWidth + PAD * 2, CELL_H + PAD * 2);

        // Icons sit on the bottom of the cell; the marker takes the space above.
        int iconY = rowY + ARROW_H;
        int x = rowX;
        for (Sample sample : samples) {
            drawIcon(graphics, font, x, iconY, sample, guiTicks);
            x += ICON + GAP;
        }
    }

    private static void drawIcon(GuiGraphics graphics, Font font, int x, int y,
                                 Sample sample, int guiTicks) {
        Stat stat = sample.stat();
        Icons.drawFilled(graphics, stat.shape(), x, y, PIXEL, sample.fraction(),
                stat.colorFor(sample.fraction(), guiTicks));

        // Saturation rides on top of the food level as a brighter wash, the way
        // DayZ distinguishes a full stomach from a full reserve.
        if (stat == Stat.FOOD && sample.saturation() > 0.0F) {
            Icons.overlayBottom(graphics, x, y, ICON, PIXEL, sample.saturation(), 0x55FFFFFF);
        }

        if (stat == Stat.XP) {
            // The level number takes the marker's place rather than sharing it. It
            // says more about experience than a chevron would, and experience only
            // ever moves one way.
            String label = Integer.toString(sample.level());
            graphics.drawString(font, label, x + (ICON - font.width(label)) / 2,
                    y - ARROW_H + 2, HudTheme.TEXT_BRIGHT, true);
            return;
        }

        if (sample.chevrons() > 0 && sample.alpha() > 0.0F) {
            HudTheme.chevrons(graphics, x + ICON / 2, y - HudTheme.CHEVRON_STACK_H - 1,
                    sample.chevrons(), sample.up(), sample.alpha());
        }
    }
}
