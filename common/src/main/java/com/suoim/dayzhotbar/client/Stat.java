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

/**
 * A status shown in the bottom-right readout, with the shape it draws and the
 * rate thresholds that decide how many chevrons its trend marker gets.
 * <p>
 * The thresholds are in each stat's own display units and are measured over one
 * second. They are deliberately low for the stats that move slowly: natural
 * health regeneration is only about 0.25 HP per second, so a threshold of 1.0
 * would never fire and the marker would never show that you were healing.
 * <p>
 * Order here is draw order, left to right.
 */
public enum Stat {
    /** Food level. Saturation is drawn over it as a secondary fill. */
    FOOD(Icons.APPLE, true, 0, 0.05F, 4.0F),

    /**
     * Armour points. Hidden when the player has none, and not tiered: armour is what
     * you are wearing rather than a warning, so it stays one colour however low it
     * gets. Losing a chestplate is not the same kind of event as running out of
     * health.
     */
    ARMOR(Icons.SHIELD, false, HudTheme.TIER_NORMAL, 0.05F, 4.0F),

    /** Air. Hidden while the player is not underwater. */
    AIR(Icons.BUBBLE, true, 0, 0.5F, 50.0F),

    /**
     * Golden absorption hearts. Hidden when there are none. Not tiered - having
     * less absorption is not a warning, so it keeps one colour throughout.
     */
    ABSORPTION(Icons.HEART, false, HudTheme.TIER_ABSORPTION, 0.05F, 3.0F),

    /**
     * Experience, drawn as a gem. Not tiered either: how far through a level you
     * are is not a health warning, and a green bar here was the one element on the
     * HUD that did not belong to the same palette as everything else.
     */
    XP(Icons.DIAMOND, false, HudTheme.TIER_NORMAL, 5.0F, 50.0F),

    /**
     * Health, or the mount's health while riding. Deliberately last so it sits at
     * the right-hand end of the row, where DayZ puts it and where the eye goes
     * first in a fight.
     */
    HEALTH(Icons.CROSS, true, 0, 0.05F, 3.0F);

    /** Below this a stat flashes. */
    private static final float TIER_CRITICAL_AT = 0.075F;
    /** Below this a stat is red. */
    private static final float TIER_RED_AT = 0.20F;
    /** Below this a stat is yellow rather than white. */
    private static final float TIER_YELLOW_AT = 0.50F;
    /** Ticks each half of the critical flash lasts - 4 is a fifth of a second. */
    private static final int FLASH_TICKS = 4;

    private final String[] shape;
    private final boolean tiered;
    private final int fixedColor;
    private final float minor;
    private final float major;

    Stat(String[] shape, boolean tiered, int fixedColor, float minor, float major) {
        this.shape = shape;
        this.tiered = tiered;
        this.fixedColor = fixedColor;
        this.minor = minor;
        this.major = major;
    }

    /** The pixel-art shape this stat draws. */
    public String[] shape() {
        return shape;
    }

    /**
     * Whether how full this stat is changes its colour. When false the stat draws
     * in {@link #fixedColor()} regardless of level.
     */
    public boolean tiered() {
        return tiered;
    }

    /** The colour used when {@link #tiered()} is false. */
    public int fixedColor() {
        return fixedColor;
    }

    /** Delta over one second at or above this shows one chevron. */
    public float minor() {
        return minor;
    }

    /** Delta over one second at or above this shows two chevrons. */
    public float major() {
        return major;
    }

    /** The colour to draw this stat's shape in, given how full it is. */
    public int colorFor(float fraction, int guiTicks) {
        return tiered ? tierColor(fraction, guiTicks) : fixedColor;
    }

    /**
     * The colour for a stat at {@code fraction} full, flashing when critical.
     * <p>
     * The bands are: healthy above half, yellow down to a fifth, red down to
     * about a thirteenth, and below that it flashes. For food on a 20-point scale
     * that puts yellow under 10, red under 4 and the flash under 1.5.
     *
     * @param guiTicks the GUI tick counter, used as the flash clock
     */
    public static int tierColor(float fraction, int guiTicks) {
        if (fraction <= TIER_CRITICAL_AT) {
            boolean lit = ((guiTicks / FLASH_TICKS) & 1) == 0;
            return lit ? HudTheme.TIER_CRITICAL : HudTheme.TIER_CRITICAL_DIM;
        }
        if (fraction <= TIER_RED_AT) {
            return HudTheme.TIER_RED;
        }
        if (fraction <= TIER_YELLOW_AT) {
            return HudTheme.TIER_YELLOW;
        }
        return HudTheme.TIER_NORMAL;
    }
}
