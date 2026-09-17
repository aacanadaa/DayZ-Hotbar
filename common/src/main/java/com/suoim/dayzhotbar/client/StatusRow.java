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
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * The bottom-right status readout: a horizontal row of icons divided into sections,
 * each stat drawn as an outlined vessel that fills from the bottom, each effect as a
 * flat white mark.
 * <p>
 * Sections are separated by an upright line, and only where both sides have something
 * in them - a section that is not laid out cannot have a line beside it, so the
 * effects divider appears and disappears with the effects themselves.
 * <p>
 * There is deliberately no panel behind the row. An earlier version put one there to
 * match the hotbar, but at this size it read as a dark slab behind the icons rather
 * than as a backing, so the icons now stand on their own over the world and the
 * outline carries them.
 * <p>
 * The row is right-aligned, so stats that come and go (air while you are on land) do
 * not shift the ones that are always there - the row simply grows and shrinks from the
 * left.
 */
public final class StatusRow {
    private StatusRow() {}

    /**
     * Height of a digit as actually drawn, shadow included. The font reports a line
     * height two pixels taller than its glyphs, which is leading rather than ink and
     * is the wrong thing to centre against.
     */
    private static final int GLYPH_HEIGHT = 8;
    /**
     * Scale the experience level number is drawn at. At full size two digits filled
     * the icon edge to edge, so the digits shrink to leave them some air.
     */
    private static final float LEVEL_TEXT_SCALE = 0.75F;
    /**
     * Pixels to drop the level number below the centre of its cell.
     * <p>
     * Centring it on the cell puts it high on the drop, because a drop's mass is in
     * its bulb and the bulb sits below the middle of the box - the thin tail at the
     * top pulls the geometric centre upward of where the shape actually looks.
     */
    private static final float LEVEL_TEXT_DROP = 3.0F;
    /**
     * Size of one source pixel of an icon. One, not two: the whole readout has to fit
     * inside the hotbar's own height, markers included, and at 2px per cell an icon
     * alone was taller than the bar.
     */
    private static final int PIXEL = 1;
    /** Icon edge length: a 15x15 shape at 1px per cell. */
    public static final int ICON = Icons.GRID * PIXEL;
    /** Gap between adjacent icons in the same section. */
    private static final int GAP = 4;
    /** Gap between sections. Wider, so the divider has room to sit in it. */
    private static final int GROUP_GAP = 11;
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

    /** Passed as {@link Cell#level()} for everything that is not experience. */
    public static final int NO_LEVEL = -1;

    /**
     * One icon in the readout.
     * <p>
     * Two kinds share this: a stat, drawn as a vessel with a fill level and possibly a
     * trend marker, and an effect mark, drawn solid. {@link #solid()} says which, and
     * the fields that do not apply to that kind are ignored - an effect has no fill
     * level, no marker and no level number, and a stat has an alpha of 1.
     *
     * @param shape       silhouette to draw
     * @param fraction    0..1 fill; an effect mark is full except for the pill
     * @param saturation  0..1 secondary fill, food only
     * @param mark        this is an effect mark rather than a stat
     * @param solid       draw the shape flat rather than as a vessel; marks only
     * @param chevrons    0, 1 or 2
     * @param up          trend direction; only meaningful when chevrons &gt; 0
     * @param markerAlpha marker opacity, so it fades rather than snapping off
     * @param level       experience level, or {@link #NO_LEVEL}
     * @param plusBadge   draw the plus in the top right, absorption only
     * @param group       which section this cell belongs to
     * @param alpha       opacity of the whole cell, used to fade an effect out
     */
    public record Cell(String[] shape, int color, float fraction, float saturation,
                       boolean mark, boolean solid, int chevrons, boolean up, float markerAlpha,
                       int level, boolean plusBadge, Group group, float alpha) {}

    public static void render(GuiGraphicsExtractor graphics, Font font, int screenWidth, int screenHeight,
                              List<Cell> cells, int guiTicks) {
        if (cells.isEmpty()) {
            return;
        }

        int width = 0;
        Group previous = null;
        for (Cell cell : cells) {
            if (previous != null) {
                width += cell.group() == previous ? GAP : GROUP_GAP;
            }
            width += ICON;
            previous = cell.group();
        }

        int x = screenWidth - RIGHT_MARGIN - width;
        int y = screenHeight - BOTTOM_MARGIN - CELL_H;

        previous = null;
        for (Cell cell : cells) {
            if (previous != null) {
                if (cell.group() == previous) {
                    x += GAP;
                } else {
                    // Divider centred in the wider gap. It is only ever reached when
                    // both sides have cells, which is exactly when it is wanted.
                    int dividerX = x + GROUP_GAP / 2;
                    graphics.fill(dividerX, y, dividerX + 1, y + ICON, HudTheme.DIVIDER);
                    x += GROUP_GAP;
                }
            }

            drawCell(graphics, font, x, y, cell, guiTicks);
            x += ICON;
            previous = cell.group();
        }
    }

    private static void drawCell(GuiGraphicsExtractor graphics, Font font, int x, int y,
                                 Cell cell, int guiTicks) {
        int color = withAlpha(cell.color(), cell.alpha());

        // Marks are drawn with the same vessel treatment as the stats - outline, gap,
        // fill - so the whole row reads as one set. What makes a mark a mark is that it
        // has no fill level of its own, no marker, no badge and no colour banding.
        if (cell.solid()) {
            Icons.drawSolid(graphics, cell.shape(), x, y, color);
        } else {
            Icons.drawVessel(graphics, cell.shape(), x, y, PIXEL, cell.fraction(), color, color);
        }

        if (cell.mark()) {
            return;
        }

        // Saturation rides on top of the food level as a brighter wash, the way DayZ
        // distinguishes a full stomach from a full reserve.
        if (cell.saturation() > 0.0F) {
            Icons.overlayBottom(graphics, x, y, ICON, PIXEL, cell.saturation(), 0x55FFFFFF);
        }

        // Absorption is drawn as a second health cross, so the plus is the only thing
        // telling the two apart. It sits in the top right, which the cross's shape
        // leaves empty.
        if (cell.plusBadge()) {
            Icons.drawSolid(graphics, Icons.PLUS, x + ICON - Icons.PLUS_SIZE, y, color);
        }

        if (cell.level() != NO_LEVEL) {
            drawLevel(graphics, font, x, y, cell.level());
        }

        if (cell.chevrons() > 0 && cell.markerAlpha() > 0.0F) {
            int markerY = cell.up()
                    ? y - HudTheme.CHEVRON_STACK_H - MARKER_GAP
                    : y + ICON + MARKER_GAP;
            HudTheme.chevrons(graphics, x + ICON / 2, markerY,
                    cell.chevrons(), cell.up(), cell.markerAlpha());
        }
    }

    /**
     * The experience level, drawn on the gem rather than beside it so experience stays
     * in step with every other stat.
     * <p>
     * Centred on the digits' own height rather than the font's line height, which
     * carries two pixels of leading under them - that two is leading, not ink - and
     * the drop shadow adds weight below the glyph on top of that. Both biases point
     * downward.
     */
    private static void drawLevel(GuiGraphicsExtractor graphics, Font font, int x, int y, int level) {
        String label = Integer.toString(level);
        float glyphHeight = GLYPH_HEIGHT * LEVEL_TEXT_SCALE;
        float labelX = x + ICON / 2.0F;
        float labelY = y + (ICON - glyphHeight) / 2.0F + LEVEL_TEXT_DROP;
        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX, labelY);
        graphics.pose().scale(LEVEL_TEXT_SCALE, LEVEL_TEXT_SCALE);
        graphics.text(font, label, -font.width(label) / 2, 0, HudTheme.TEXT_BRIGHT, true);
        graphics.pose().popMatrix();
    }

    /**
     * Scales a colour's own alpha by {@code factor} rather than replacing it.
     * <p>
     * Replacing it looked equivalent and was not: the critical flash alternates between
     * a bright red and a half-transparent one, and overwriting that half-transparent
     * alpha with an opaque one made both phases the same colour. The icon stopped
     * flashing while still going red - which is exactly how it was reported.
     */
    private static int withAlpha(int argb, float factor) {
        float clamped = Math.max(0.0F, Math.min(1.0F, factor));
        int a = Math.round((argb >>> 24) * clamped);
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
