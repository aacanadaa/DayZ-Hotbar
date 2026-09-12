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
 * Tracks how fast a stat is moving, so the HUD can show a DayZ-style trend arrow.
 * <p>
 * Samples are pushed once per client tick into a fixed window, and the delta
 * across that window is compared against two thresholds:
 * <ul>
 *   <li>at or above {@code minor} - one chevron</li>
 *   <li>at or above {@code major} - two chevrons</li>
 * </ul>
 * This is what makes a poison tick or a regeneration effect read differently from
 * ordinary background drift, without any special-casing per stat.
 * <p>
 * The arrow lingers for a short hold after the movement stops, then fades, so it
 * is legible rather than a one-frame flicker.
 */
public final class VelocityTracker {
    /** Ticks of history the delta is measured over - one second at 20 tps. */
    private static final int WINDOW = 20;
    /**
     * Ticks the marker stays up after movement is last seen. Long on purpose, and
     * raised twice: at 15 ticks a short exchange of damage came and went before the
     * eye caught it, and the whole point of the marker is that you notice the trend
     * without staring. Two and a half seconds is long enough to glance away and back.
     */
    private static final int HOLD = 50;
    /**
     * Ticks before the end of the hold at which a double marker drops to a single one.
     * The alpha fade below starts at {@link #FADE}, so the last stretch of a double
     * marker's life runs two chevrons, then one, then one fading out, then nothing.
     */
    private static final int STEP_DOWN = 20;
    /** Ticks the marker spends fading out at the end of the hold. */
    private static final int FADE = 10;

    private final float minor;
    private final float major;
    private final float[] history = new float[WINDOW];

    private int head = 0;
    private int samples = 0;
    private float previous = Float.NaN;

    private int peak = 0;
    private boolean up = false;
    private int holdTicks = 0;

    public VelocityTracker(float minor, float major) {
        this.minor = minor;
        this.major = major;
    }

    /** Records a sample. Call once per client tick. */
    public void push(float value) {
        if (Float.isNaN(previous)) {
            // First sample: seed the whole window so the first delta is zero
            // rather than a spike from the default 0.0.
            this.previous = value;
            java.util.Arrays.fill(history, value);
            this.samples = WINDOW;
            return;
        }

        history[head] = value;
        head = (head + 1) % WINDOW;
        if (samples < WINDOW) {
            samples++;
        }

        float oldest = history[head]; // the sample being overwritten next
        float delta = value - oldest;
        float magnitude = Math.abs(delta);

        if (magnitude >= major) {
            peak = 2;
            up = delta > 0.0F;
            holdTicks = HOLD;
        } else if (magnitude >= minor) {
            peak = 1;
            up = delta > 0.0F;
            holdTicks = HOLD;
        }

        this.previous = value;
    }

    /** Advances the hold/fade timers. Call once per client tick. */
    public void tick() {
        if (holdTicks > 0) {
            holdTicks--;
        }
    }

    /**
     * 0, 1 or 2 chevrons.
     * <p>
     * A double marker steps down to a single one before it goes, rather than just
     * fading out at two. The count is the part that carries meaning - two means
     * "something significant is happening" - so easing it off by degree reads as the
     * event passing rather than as the HUD flickering.
     */
    public int chevrons() {
        if (holdTicks <= 0) {
            return 0;
        }
        if (peak == 2 && holdTicks <= STEP_DOWN) {
            return 1;
        }
        return peak;
    }

    /** True when the trend is upward. Only meaningful when {@link #chevrons()} is non-zero. */
    public boolean up() {
        return up;
    }

    /**
     * Alpha multiplier for the arrow, 0.0 to 1.0, so it fades rather than vanishing.
     */
    public float alpha() {
        if (holdTicks <= 0) {
            return 0.0F;
        }
        if (holdTicks >= FADE) {
            return 1.0F;
        }
        return holdTicks / (float) FADE;
    }

    /** Drops all history, e.g. when the player respawns or the world changes. */
    public void reset() {
        previous = Float.NaN;
        head = 0;
        samples = 0;
        peak = 0;
        holdTicks = 0;
    }
}
