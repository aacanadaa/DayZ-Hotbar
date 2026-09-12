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
package com.suoim.dayzhotbar.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

/**
 * The bottom-right status readout: a horizontal row of icons, each drawn as an
 * outlined vessel that fills from the bottom, each with a flat rank-chevron trend
 * marker on the side it is heading towards.
 * <p>
 * There is deliberately no panel behind the row. An earlier version put one there to
 * match the hotbar, but at this size it read as a dark slab behind the icons rather
 * than as a backing, so the icons now stand on their own over the world and the
 * outline carries them.
 * <p>
 * The row is right-aligned, so stats that come and go (armour you are not wearing,
 * air while you are on land) do not shift the ones that are always there - the row
 * simply grows and shrinks from the left.
 */
public final class StatusRow {
    private StatusRow() {}

    /**
     * Size of one source pixel of an icon. One, not two: the whole readout has to fit
     * inside the hotbar's own height, markers included, and at 2px per cell an icon
     * alone was taller than the bar.
     */
    private static final int PIXEL = 1;
    /** Icon edge length: a 15x15 shape at 1px per cell. */
    public static final int ICON = Icons.GRID * PIXEL;
    /** Gap between adjacent icons. */
    private static final int GAP = 4;
    /**
     * Space reserved below the icons for a falling marker. A rising marker draws
     * above the icon instead, into space that is free anyway, so only this side has
     * to be budgeted for - which is what lets the icons stay down by the hotbar.
     */
    public static final int ARROW_H = HudTheme.CHEVRON_STACK_H + 1;
    /** Total cell height: the icons, plus the marker space underneath them. */
    public static final int CELL_H = ICON + ARROW_H;
    /** Gap between an icon's edge and its marker. */
    private static final int MARKER_GAP = 1;
    /**
     * Distance from the bottom screen edge. Matches the hotbar so the two stay on one
     * baseline.
     */
    private static final int BOTTOM_MARGIN = 10;
    /**
     * Distance from the right screen edge. Larger than the bottom margin on purpose:
     * at the same inset the row sat flush against the edge of the screen, which read
     * as clipped rather than placed.
     */
    private static final int RIGHT_MARGIN = 16;

    /**
     * One rendered stat: how full it is, and what its trend marker should say.
     *
     * @param fraction   0..1 fill amount
     * @param saturation 0..1 secondary fill, only used by food
     * @param chevrons   0, 1 or 2
     * @param up         trend direction; only meaningful when chevrons &gt; 0
     * @param alpha      marker opacity, so it fades rather than snapping off
     * @param level      experience level, drawn on the XP icon
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
        int rowX = screenWidth - RIGHT_MARGIN - rowWidth;
        int rowY = screenHeight - BOTTOM_MARGIN - CELL_H;

        int x = rowX;
        for (Sample sample : samples) {
            drawIcon(graphics, font, x, rowY, sample, guiTicks);
            x += ICON + GAP;
        }
    }

    private static void drawIcon(GuiGraphics graphics, Font font, int x, int y,
                                 Sample sample, int guiTicks) {
        Stat stat = sample.stat();

        // Outline and fill take the same colour, so a yellow icon has a yellow
        // outline. The gap between them is what keeps the two readable apart.
        int color = stat.colorFor(sample.fraction(), guiTicks);
        Icons.drawVessel(graphics, stat.shape(), x, y, PIXEL, sample.fraction(), color, color);

        // Saturation rides on top of the food level as a brighter wash, the way
        // DayZ distinguishes a full stomach from a full reserve.
        if (stat == Stat.FOOD && sample.saturation() > 0.0F) {
            Icons.overlayBottom(graphics, x, y, ICON, PIXEL, sample.saturation(), 0x55FFFFFF);
        }

        if (stat == Stat.XP) {
            // The level number sits on the icon rather than beside it, which keeps
            // experience in step with every other stat instead of being the one that
            // needed extra room for a label.
            String label = Integer.toString(sample.level());
            graphics.drawString(font, label, x + (ICON - font.width(label)) / 2,
                    y + (ICON - font.lineHeight) / 2 + 1, HudTheme.TEXT_BRIGHT, true);
        }

        if (sample.chevrons() > 0 && sample.alpha() > 0.0F) {
            // The marker goes on the side the stat is heading: above when it is
            // rising, below when it is falling. Only the space below the icons is
            // reserved - a rising marker draws into the open space above, which
            // costs the layout nothing and keeps the icons down near the hotbar.
            int markerY = sample.up()
                    ? y - HudTheme.CHEVRON_STACK_H - MARKER_GAP
                    : y + ICON + MARKER_GAP;
            HudTheme.chevrons(graphics, x + ICON / 2, markerY,
                    sample.chevrons(), sample.up(), sample.alpha());
        }
    }
}
