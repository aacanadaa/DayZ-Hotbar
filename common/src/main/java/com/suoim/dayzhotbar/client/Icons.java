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

import net.minecraft.client.gui.GuiGraphics;

/**
 * The status icons, drawn as pixel art rather than borrowed from a sheet.
 * <p>
 * Each icon is a 15x15 character grid where {@code #} marks a cell of the shape.
 * Every shape is drawn in three derived layers rather than as a solid mass:
 * <ol>
 *   <li>an <b>outline</b> - the cells on the shape's edge, always drawn;</li>
 *   <li>a one-cell <b>gap</b>, left transparent, so the fill never touches the
 *       outline and the two stay readable apart;</li>
 *   <li>the <b>fill</b> - everything left over, drawn up to the current level.</li>
 * </ol>
 * The layers are computed from the mask by erosion, so an icon is authored as a
 * single silhouette and the outline and gap come out of it for free.
 * <p>
 * The grid is 15x15. It was 9x9, then 11x11, and both were too coarse: an outline
 * and a gap take two cells off every edge, so at 11x11 a shape like the shield had
 * barely any contour left and read as a blob. The apple survived the smaller grids
 * because a round shape with a straight stem is the one silhouette that does not
 * need many cells to say what it is.
 */
public final class Icons {
    private Icons() {}

    /** Source grid dimension. Every shape below is exactly this wide and tall. */
    public static final int GRID = 15;

    /**
     * A heart. Two lobes with a V between them, tapering to a point.
     * <p>
     * The notch is what makes it a heart and it has to cut three rows deep. At one or
     * two the lobes fill in on the next row down and the figure is a blob with a dent
     * in the top - which is what an earlier version was.
     */
    public static final String[] HEART = {
        "..###.....###..",
        ".#####...#####.",
        "#######.#######",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        ".#############.",
        "..###########..",
        "...#########...",
        "....#######....",
        ".....#####.....",
        "......###......",
        ".......#......."
    };

    /**
     * An apple: a round body with a stem and a leaf on top.
     * <p>
     * A dip at the top with the stem over it was tried and taken back out. It sounds
     * right - apples do have a dip - but at this size the notch reads as the handle of
     * a basket, and the fruit turned into a bag. The round body with the stem simply
     * sitting on it is the silhouette that reads, and it is what the emoji looks like
     * once the shading is gone.
     */
    public static final String[] APPLE = {
        "......#........",
        "......#.##.....",
        "...########....",
        "..##########...",
        ".############..",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        ".#############.",
        "..###########..",
        "...#########...",
        "....#######....",
        ".....#####....."
    };

    /**
     * A heater shield: a domed top, sides that stay vertical through the middle, and
     * a taper confined to the bottom third that ends on a flat edge.
     * <p>
     * Both extremes were wrong. Tapering evenly from the top gives a funnel; ending
     * in a single cell gives a spike; a flat top over eight rows gives a rectangle
     * with a point bolted on. It also began life as a chestplate, but shoulders drawn
     * as {@code .##...##.} over a taper read as a heart - the same silhouette the
     * health icon used to have.
     * <p>
     * <b>Not currently drawn.</b> Armour was taken out of the status row on request
     * and the shape is kept here for wherever it goes next.
     */
    public static final String[] SHIELD = {
        "...#########...",
        "..###########..",
        ".#############.",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        ".#############.",
        "..###########..",
        "..###########..",
        "...#########...",
        "....#######....",
        ".....#####....."
    };

    /** A bubble. */
    public static final String[] BUBBLE = {
        ".....#####.....",
        "...#########...",
        "..###########..",
        ".#############.",
        ".#############.",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        ".#############.",
        ".#############.",
        "..###########..",
        "...#########...",
        ".....#####....."
    };

    /**
     * A medical cross, used for health. Drawn in the tier colour rather than being
     * permanently red, so it still goes yellow and flashes as health drops - a cross
     * that was always red would say nothing about how much health is left.
     * <p>
     * All four arms are seven cells thick and four cells long, so the figure is
     * symmetric in both directions. It was not, briefly: the vertical arm was seven
     * wide while the horizontal band was only five tall, which made the top and
     * bottom arms visibly wider than the sides.
     * <p>
     * A nine-cell version was also tried and reverted - it made the bar across the
     * middle too heavy.
     */
    public static final String[] CROSS = {
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "####+#####+####",
        "###############",
        "###############",
        "###############",
        "###############",
        "###############",
        "####+#####+####",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######...."
    };

    /**
     * A broken heart, the mark for a harmful effect. The heart split down the middle
     * by a crack, which pairs with the whole heart used for beneficial effects - the
     * two read as a set, and a heart with a break in it says "afflicted" without
     * being directional the way an arrow would be.
     */
    public static final String[] BROKEN_HEART = {
        "..###.....###..",
        ".#####...#####.",
        "#######.#######",
        "######..#######",
        "######..#######",
        "#######.#######",
        "#######..######",
        "#######..######",
        ".######..#####.",
        "..#####.#####..",
        "...####.####...",
        "....#######....",
        ".....#####.....",
        "......###......",
        ".......#......."
    };

    /**
     * A capsule, the mark for a restorative effect. Filled to half, which is what makes
     * it read as a pill rather than as a lozenge.
     * <p>
     * Upright, and that is the only orientation that works here. A capsule on the
     * diagonal was tried at three widths and read as a needle every time: a tilted
     * capsule needs its rows to span its width times the root of two, so one fat enough
     * to read needs rows wider than the run left to taper over, and the ends come out
     * as points. Upright it has room to be both blunt-ended and wide.
     * <p>
     * It also puts the half on the right axis. The fill is the lower half of whatever
     * the shape occupies, so upright a pill reads as half used; laid on its side the
     * same fill cut across the capsule, which is not how a pill is ever drawn.
     */
    public static final String[] PILL = {
        "...............",
        ".....#####.....",
        "....#######....",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "....#######....",
        ".....#####.....",
        "..............."
    };

    /**
     * A drop, used for experience. Pointed at the top, round at the bottom, the shape
     * DayZ uses for blood.
     */
    public static final String[] DROP = {
        ".......#.......",
        "......###......",
        ".....#####.....",
        ".....#####.....",
        "....#######....",
        "....#######....",
        "...#########...",
        "..###########..",
        "..###########..",
        "..###########..",
        "..###########..",
        "..###########..",
        "...#########...",
        "....#######....",
        ".....#####....."
    };

    /**
     * A small plus, badged onto the absorption cross to distinguish it from health.
     * Five cells square, drawn a screen pixel per cell rather than at the icons'
     * scale, so it stays a mark rather than a second figure.
     */
    public static final String[] PLUS = {
        "..#..",
        "..#..",
        "#####",
        "..#..",
        "..#.."
    };

    /** Edge length of {@link #PLUS}. */
    public static final int PLUS_SIZE = PLUS.length;

    /**
     * Draws a shape filled solid, one screen pixel per cell, with no outline or
     * interior of its own. For the small plus badge on the absorption cross, which is
     * a mark laid over another icon rather than a figure in its own right.
     */
    public static void drawSolid(GuiGraphics graphics, String[] shape, int x, int y, int color) {
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length(); col++) {
                if (shape[row].charAt(col) == '#') {
                    graphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
                }
            }
        }
    }

    // --- Marks for the player panel ---------------------------------------------
    // Authored eleven cells square rather than on the fifteen-cell status grid: they
    // sit beside one line of text, where a fifteen-cell figure is taller than the text
    // it labels.

    /**
     * A standing figure, the stance mark for walking. One of three: the same figure
     * with its legs straight, folded under it, or stretched out mid-stride.
     */
    public static final String[] STANCE_WALK = {
        "....###....",
        "....###....",
        "...........",
        "...#####...",
        "...#####...",
        "...#####...",
        "..##...##..",
        "..#.....#..",
        "..#.....#..",
        "..#.....#..",
        "..........."
    };

    /** The same figure crouched: body lower, legs folded out to the sides. */
    public static final String[] STANCE_CROUCH = {
        "...........",
        "....###....",
        "....###....",
        "...........",
        "...#####...",
        "..#######..",
        "..#######..",
        ".##.....##.",
        ".##.....##.",
        "...........",
        "..........."
    };

    /** The same figure mid-stride: body forward, legs stretched apart. */
    public static final String[] STANCE_RUN = {
        "...###.....",
        "...###.....",
        "...........",
        "..#####....",
        "..#####....",
        "..####.....",
        ".###..##...",
        "##.....##..",
        "#.......##.",
        "...........",
        "..........."
    };

    /**
     * A shield, eleven cells, sized to sit level with the stance figures.
     * <p>
     * The taper is confined to the lower half. Running it evenly from the top, as the
     * first attempt did, gives a straight-sided wedge - a triangle, not a shield.
     */
    public static final String[] SHIELD_MARK = {
        "###########",
        "###########",
        "###########",
        "###########",
        "###########",
        ".#########.",
        ".#########.",
        "..#######..",
        "..#######..",
        "...#####...",
        "...#####..."
    };

    /**
     * A plastic bottle, the water mark: cap and neck as one straight column over a
     * wider body.
     * <p>
     * The neck does not pinch in below the cap. It was drawn narrower at first, on the
     * reasoning that a real bottle narrows there, and at this size it read as a dent in
     * the side rather than as a neck.
     */
    public static final String[] WATER_BOTTLE = {
        ".....#####.....",
        ".....#####.....",
        ".....#####.....",
        ".....#####.....",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        "...#########...",
        ".....#####....."
    };

    /**
     * A thermometer, the temperature mark: a narrow tube over a bulb.
     * <p>
     * The tube is five cells so that an outline and a gap still leave a cell of
     * interior to fill; at three there would be nothing left to show a level with.
     */
    public static final String[] THERMOMETER = {
        ".....#####.....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "....#######....",
        "..###########..",
        ".#############.",
        ".#############.",
        "..###########..",
        "....#######...."
    };

    /** Draws one shape's cells in a single flat colour. */
    private static void plot(GuiGraphics graphics, boolean[][] cells, int x, int y, int pixel, int color) {
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                if (cells[row][col]) {
                    int px = x + col * pixel;
                    int py = y + row * pixel;
                    graphics.fill(px, py, px + pixel, py + pixel, color);
                }
            }
        }
    }

    /**
     * Cells of the shape. {@code +} counts as part of the shape and additionally
     * forces that cell to be drawn as outline - see {@link #outlineOf}.
     */
    private static boolean[][] mask(String[] shape) {
        int n = shape.length;
        boolean[][] m = new boolean[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                char c = shape[row].charAt(col);
                m[row][col] = c == '#' || c == '+';
            }
        }
        return m;
    }

    /**
     * The cells the shape author marked {@code +}, which are drawn as outline even
     * though the edge rule would not pick them.
     * <p>
     * This exists because a sharp concave corner and a step on a curve are the same
     * pattern at the cell level, so no rule can fill one without wrecking the other.
     * Marking them per shape is the only precise option, and it costs one character
     * where the corner actually is.
     */
    private static boolean[][] forcedOf(String[] shape) {
        int n = shape.length;
        boolean[][] forced = new boolean[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                forced[row][col] = shape[row].charAt(col) == '+';
            }
        }
        return forced;
    }

    private static boolean inside(boolean[][] m, int row, int col) {
        return row >= 0 && row < m.length && col >= 0 && col < m.length && m[row][col];
    }

    /**
     * Cells on the edge of the shape: shape cells with an orthogonal neighbour
     * outside it.
     * <p>
     * Do <b>not</b> extend this to fill in concave corners. It was tried, to close the
     * diagonal step the outline makes where a cross's arm meets its body, and it
     * wrecked every other icon: a curve drawn on a grid is a staircase, and every step
     * of a staircase is a concave corner by that definition. The diamond's outline
     * went from 28 cells to 52 and the heart's from 34 to 55, so every curved icon
     * ended up with a two-cell-thick border. Sharp corners are marked by hand instead,
     * and this stays the pure edge rule so the fill is measured against the shape's
     * real contour.
     */
    private static boolean[][] outlineOf(boolean[][] m) {
        int n = m.length;
        boolean[][] out = new boolean[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (!m[row][col]) {
                    continue;
                }
                out[row][col] = !inside(m, row - 1, col) || !inside(m, row + 1, col)
                        || !inside(m, row, col - 1) || !inside(m, row, col + 1);
            }
        }
        return out;
    }

    private static boolean[][] union(boolean[][] a, boolean[][] b) {
        int n = a.length;
        boolean[][] out = new boolean[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                out[row][col] = a[row][col] || b[row][col];
            }
        }
        return out;
    }

    /**
     * The interior worth filling: shape cells that are neither outline nor touching
     * an outline cell. The untouched ring between the two is the gap.
     */
    private static boolean[][] fillOf(boolean[][] m, boolean[][] outline) {
        int n = m.length;
        boolean[][] fill = new boolean[n][n];
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (!m[row][col] || outline[row][col] || touches(outline, row, col)) {
                    continue;
                }
                fill[row][col] = true;
            }
        }
        return fill;
    }

    private static boolean touches(boolean[][] outline, int row, int col) {
        return at(outline, row - 1, col) || at(outline, row + 1, col)
                || at(outline, row, col - 1) || at(outline, row, col + 1);
    }

    private static boolean at(boolean[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid.length && grid[row][col];
    }

    /**
     * Draws a shape as a vessel: a static outline, a clear gap, and an interior that
     * fills from the bottom up.
     * <p>
     * The fill is measured against the interior's own vertical extent rather than the
     * icon's, so a shape that does not reach the bottom of its grid still fills from
     * empty to full across exactly the range it occupies.
     *
     * @param fraction 0.0 (empty) to 1.0 (full); values outside are clamped
     */
    public static void drawVessel(GuiGraphics graphics, String[] shape, int x, int y, int pixel,
                                  float fraction, int outlineColor, int fillColor) {
        boolean[][] m = mask(shape);
        // The fill is measured against the outline as actually drawn, marked corners
        // included. Measuring it against the pure edge instead was tried, to stop the
        // corners eating a pixel of fill - it worked, but the fill then ran right up
        // against the outline with no gap left between them, which reads worse than
        // the narrower fill did. The gap matters more than the last pixel of width.
        boolean[][] outline = union(outlineOf(m), forcedOf(shape));
        boolean[][] fill = fillOf(m, outline);

        plot(graphics, outline, x, y, pixel, outlineColor);

        if (fraction <= 0.0F) {
            return;
        }

        int top = -1;
        int bottom = -1;
        for (int row = 0; row < fill.length; row++) {
            for (int col = 0; col < fill[row].length; col++) {
                if (fill[row][col]) {
                    if (top < 0) {
                        top = row;
                    }
                    bottom = row;
                    break;
                }
            }
        }
        if (top < 0) {
            return;
        }

        int regionTop = y + top * pixel;
        int regionBottom = y + (bottom + 1) * pixel;
        int regionHeight = regionBottom - regionTop;
        int filled = Math.round(regionHeight * Math.min(1.0F, fraction));

        if (filled <= 0) {
            return;
        }
        if (filled >= regionHeight) {
            plot(graphics, fill, x, y, pixel, fillColor);
            return;
        }

        // The shape's own width, not the grid constant: marks are authored at smaller
        // sizes than the status icons and would otherwise be clipped to the wrong box.
        graphics.enableScissor(x, regionBottom - filled, x + m.length * pixel, regionBottom);
        plot(graphics, fill, x, y, pixel, fillColor);
        graphics.disableScissor();
    }

    /**
     * A translucent wash over the bottom {@code fraction} of the interior, used to
     * show food saturation on top of the food level.
     */
    public static void overlayBottom(GuiGraphics graphics, int x, int y, int size, int pixel,
                                     float fraction, int color) {
        if (fraction <= 0.0F) {
            return;
        }
        int filled = Math.max(1, Math.round(size * Math.min(1.0F, fraction)));
        graphics.fill(x + pixel * 2, y + size - filled, x + size - pixel * 2, y + size - pixel * 2,
                color);
    }
}
