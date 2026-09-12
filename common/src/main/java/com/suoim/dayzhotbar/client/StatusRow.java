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
 * The bottom-right status readout: a horizontal row of icons, each filled to its
 * current level, each with a trend arrow underneath.
 * <p>
 * The row is right-aligned, so stats that come and go (armour you are not
 * wearing, air while you are on land) do not shift the ones that are always
 * there - the row simply grows and shrinks from the left.
 */
public final class StatusRow {
    private StatusRow() {}

    /** Icon edge length. Twice the 9px source tile, which keeps the pixels crisp. */
    public static final int ICON = 18;
    /** Gap between adjacent icons. */
    public static final int GAP = 6;
    /** Vertical space reserved below the icons for the trend arrows. */
    public static final int ARROW_H = 10;
    /** Vertical space reserved above the icons, used by the XP level number. */
    public static final int HEADROOM = 9;
    /** Total cell height. */
    public static final int CELL_H = HEADROOM + ICON + ARROW_H;
    /** Distance from the right and bottom screen edges. */
    public static final int MARGIN = 6;

    /** Vanilla's experience-bar green. */
    private static final int XP_GREEN = 0xFF80FF20;

    /**
     * One rendered stat: how full it is, and what its trend arrow should say.
     *
     * @param fraction  0..1 fill amount
     * @param saturation 0..1 secondary fill, only used by food
     * @param chevrons   0, 1 or 2
     * @param up         trend direction; only meaningful when chevrons &gt; 0
     * @param alpha      arrow opacity, so it fades rather than snapping off
     * @param level      experience level, only used by XP
     */
    public record Sample(Stat stat, float fraction, float saturation,
                         int chevrons, boolean up, float alpha, int level) {}

    public static void render(GuiGraphics graphics, Font font, int screenWidth, int screenHeight,
                              List<Sample> samples) {
        if (samples.isEmpty()) {
            return;
        }

        int count = samples.size();
        int rowWidth = count * ICON + (count - 1) * GAP;
        int x = screenWidth - MARGIN - rowWidth;
        int iconY = screenHeight - MARGIN - CELL_H + HEADROOM;

        for (Sample sample : samples) {
            drawIcon(graphics, font, x, iconY, sample);
            x += ICON + GAP;
        }
    }

    private static void drawIcon(GuiGraphics graphics, Font font, int x, int y, Sample sample) {
        Stat stat = sample.stat();

        if (stat.usesSprite()) {
            IconSprites.fill(graphics, x, y, ICON,
                    stat.uEmpty(), stat.vEmpty(), stat.uFull(), stat.vFull(), sample.fraction());
        } else {
            drawExperience(graphics, font, x, y, sample);
        }

        // Saturation rides on top of the food level as a brighter wash, the way
        // DayZ distinguishes a full stomach from a full reserve.
        if (stat == Stat.FOOD && sample.saturation() > 0.0F) {
            IconSprites.overlay(graphics, x, y, ICON, sample.saturation(), 0x55FFFFFF);
        }

        if (sample.chevrons() > 0 && sample.alpha() > 0.0F) {
            int color = withAlpha(sample.up() ? HudTheme.STATE_ACTIVE : HudTheme.ACCENT_RED, sample.alpha());
            HudTheme.chevrons(graphics, x + ICON / 2, y + ICON + 2,
                    sample.chevrons(), sample.up(), color);
        }
    }

    /**
     * Experience has no sprite on the sheet, so it is drawn as a narrow bar with
     * the level number sitting in the headroom above it.
     */
    private static void drawExperience(GuiGraphics graphics, Font font, int x, int y, Sample sample) {
        String label = Integer.toString(sample.level());
        graphics.drawString(font, label, x + (ICON - font.width(label)) / 2, y - HEADROOM + 1,
                HudTheme.TEXT_BRIGHT, true);

        int barWidth = 8;
        int barX = x + (ICON - barWidth) / 2;
        HudTheme.panel(graphics, barX, y, barWidth, ICON);
        graphics.fill(barX + 1, y + 1, barX + barWidth - 1, y + ICON - 1, HudTheme.SLOT_INNER);

        if (sample.fraction() > 0.0F) {
            int filled = Math.max(1, Math.round((ICON - 2) * Math.min(1.0F, sample.fraction())));
            graphics.fill(barX + 1, y + ICON - 1 - filled, barX + barWidth - 1, y + ICON - 1, XP_GREEN);
        }
    }

    private static int withAlpha(int argb, float alpha) {
        int a = Math.round(255.0F * Math.max(0.0F, Math.min(1.0F, alpha)));
        return (a << 24) | (argb & 0x00FFFFFF);
    }
}
