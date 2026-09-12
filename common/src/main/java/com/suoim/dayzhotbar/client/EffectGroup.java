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

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * The families active potion effects are collapsed into for the readout.
 * <p>
 * One mark per family rather than one per effect. Minecraft has thirty-odd effects and
 * a player can hold a handful at once; a row that grew per effect would eat the whole
 * screen edge. What matters at a glance is which <em>kinds</em> of thing are on you,
 * not the exact list - the inventory screen still shows that.
 * <p>
 * The marks are drawn white with no fill level, no colour banding and no trend marker,
 * because an effect is either on or off. Losing one fades its mark out rather than
 * cutting it.
 */
public enum EffectGroup {
    /** Beneficial effects that are not about health: speed, strength, night vision. */
    GOOD(Icons.STAR, null),

    /** Effects that restore or buffer health: regeneration, absorption, saturation. */
    RECOVERY(Icons.PILL, Icons.PILL_FILLED),

    /** Everything harmful. One family, whether it is poison or mining fatigue. */
    AFFLICTION(Icons.CORONA, null);

    private final String[] shape;
    private final String[] filledHalf;

    EffectGroup(String[] shape, String[] filledHalf) {
        this.shape = shape;
        this.filledHalf = filledHalf;
    }

    /** The mark drawn for this family. */
    public String[] shape() {
        return shape;
    }

    /**
     * The part of the mark drawn solid while the rest stays hollow, or null when the
     * whole mark is solid. Only the pill uses it - a capsule with one half filled is
     * what makes it read as a pill rather than as a lozenge.
     */
    public String[] filledHalf() {
        return filledHalf;
    }

    /**
     * Which family an effect belongs to.
     * <p>
     * Recovery is carved out of the beneficial effects by name because there is no
     * flag for it - the game only distinguishes beneficial from harmful, and "this one
     * is keeping me alive" is worth separating from "this one is making me faster".
     */
    public static EffectGroup of(MobEffect effect) {
        if (effect == MobEffects.REGENERATION
                || effect == MobEffects.HEAL
                || effect == MobEffects.HEALTH_BOOST
                || effect == MobEffects.ABSORPTION
                || effect == MobEffects.SATURATION) {
            return RECOVERY;
        }
        return effect.isBeneficial() ? GOOD : AFFLICTION;
    }
}
