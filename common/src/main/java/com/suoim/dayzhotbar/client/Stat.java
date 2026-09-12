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

/**
 * A status shown in the bottom-right readout, with the shape it draws, the bands it
 * changes colour at, and the rate thresholds that decide how many chevrons its trend
 * marker gets.
 * <p>
 * The rate thresholds are in each stat's own display units and are measured over one
 * second. They are deliberately low for the stats that move slowly: natural health
 * regeneration is only about 0.25 HP per second, so a threshold of 1.0 would never
 * fire and the marker would never show that you were healing.
 * <p>
 * Order here is draw order, left to right.
 */
public enum Stat {
    /**
     * Food level. Saturation is drawn over it as a secondary fill.
     * <p>
     * Bands are DayZ's, which are quoted against a 5,000-point reserve: white from
     * 16%, yellow from 6%, red from 2%, flashing under 2%. Minecraft's food bar is
     * only 20 points, so on that scale the bands land at yellow on 3 or less, red on
     * 1, and the flash on empty.
     */
    FOOD(Icons.APPLE, new Tiers(0.019F, 0.05F, 0.15F), 0, 0.05F, 4.0F),

    /**
     * Armour has no row entry. It was here as a shield and was removed on request -
     * the shape is still in {@link Icons} for wherever it lands next. Armour is what
     * you are wearing rather than a warning, so whenever it returns it should not be
     * colour-tiered.
     */

    /**
     * Air. Hidden while the player is not underwater.
     * <p>
     * No bands were given for air, so it borrows health's. Drowning and bleeding out
     * are the same kind of emergency, and both are on a scale where the last tenth is
     * the part that matters.
     */
    AIR(Icons.BUBBLE, new Tiers(0.14F, 0.30F, 0.60F), 0, 0.5F, 50.0F),

    /**
     * Golden absorption hearts. Hidden when there are none. Not tiered - having less
     * absorption is not a warning, so it keeps one colour throughout.
     */
    ABSORPTION(Icons.HEART, null, HudTheme.TIER_ABSORPTION, 0.05F, 3.0F),

    /**
     * Experience, drawn as a gem. Not tiered either: how far through a level you are
     * is not a health warning, and a green bar here was the one element on the HUD
     * that did not belong to the same palette as everything else.
     */
    XP(Icons.DIAMOND, null, HudTheme.TIER_NORMAL, 5.0F, 50.0F),

    /**
     * Health, or the mount's health while riding. Deliberately last so it sits at the
     * right-hand end of the row, where DayZ puts it and where the eye goes first in a
     * fight.
     * <p>
     * Bands are DayZ's, quoted against 100 HP: white from 61%, yellow from 31%, red
     * from 15%, flashing under 15%. Minecraft's health bar is 20 points, so on that
     * scale the bands land at yellow on 12 or less, red on 6 or less, and the flash
     * under 3.
     */
    HEALTH(Icons.CROSS, new Tiers(0.14F, 0.30F, 0.60F), 0, 0.05F, 3.0F);

    /** Ticks each half of the critical flash lasts - 4 is a fifth of a second. */
    private static final int FLASH_TICKS = 4;

    /**
     * The colour bands for a tiered stat, given as the <em>top</em> of each band.
     * <p>
     * DayZ quotes these as inclusive ranges - health is red from 15% to 30% and
     * yellow from 31% to 60% - so the boundaries are the tops, not the bottoms of the
     * next band down. Expressing them the other way round put 30% in yellow and 60% in
     * white, one band too generous at each step.
     * <p>
     * They are fractions rather than absolute values so the same bands apply whatever
     * scale the stat is measured on: DayZ quotes health against 100 and food against
     * 5,000, while Minecraft uses 20 for both, and the intended behaviour is the same
     * either way.
     *
     * @param flashTop  at or under this the icon flashes
     * @param redTop    at or under this it is red
     * @param yellowTop at or under this it is yellow; above it, white
     */
    public record Tiers(float flashTop, float redTop, float yellowTop) {
        public int colorFor(float fraction, int guiTicks) {
            if (fraction <= flashTop) {
                boolean lit = ((guiTicks / FLASH_TICKS) & 1) == 0;
                return lit ? HudTheme.TIER_CRITICAL : HudTheme.TIER_CRITICAL_DIM;
            }
            if (fraction <= redTop) {
                return HudTheme.TIER_RED;
            }
            if (fraction <= yellowTop) {
                return HudTheme.TIER_YELLOW;
            }
            return HudTheme.TIER_NORMAL;
        }
    }

    private final String[] shape;
    private final Tiers tiers;
    private final int fixedColor;
    private final float minor;
    private final float major;

    Stat(String[] shape, Tiers tiers, int fixedColor, float minor, float major) {
        this.shape = shape;
        this.tiers = tiers;
        this.fixedColor = fixedColor;
        this.minor = minor;
        this.major = major;
    }

    /** The pixel-art shape this stat draws. */
    public String[] shape() {
        return shape;
    }

    /**
     * Whether how full this stat is changes its colour. When false the stat draws in
     * {@link #fixedColor()} regardless of level.
     */
    public boolean tiered() {
        return tiers != null;
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
        return tiers != null ? tiers.colorFor(fraction, guiTicks) : fixedColor;
    }
}
