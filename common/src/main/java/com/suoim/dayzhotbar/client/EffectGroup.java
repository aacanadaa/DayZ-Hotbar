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
    GOOD(Icons.HEART, 1.0F, false),

    /** Effects that restore or buffer health: regeneration, absorption, saturation. */
    RECOVERY(Icons.PILL, 0.5F, false),

    /** Everything harmful. One family, whether it is poison or mining fatigue. */
    AFFLICTION(Icons.BROKEN_HEART, 1.0F, false);

    private final String[] shape;
    private final float fill;
    private final boolean solid;

    EffectGroup(String[] shape, float fill, boolean solid) {
        this.shape = shape;
        this.fill = fill;
        this.solid = solid;
    }

    /** The mark drawn for this family. */
    public String[] shape() {
        return shape;
    }

    /**
     * Draw the shape flat rather than as a vessel.
     * <p>
     * The crack is the one mark that needs it. A vessel gives its outline and its
     * fill a cell of clear space apart, which is fine for a solid figure but turns two
     * halves of a broken shape into two ringed halves - a waffle. Flat, the break is
     * the only thing in the figure and reads at a glance.
     */
    public boolean solid() {
        return solid;
    }

    /**
     * How much of the mark's interior is filled. Half for the pill, because a capsule
     * with one half filled is what makes it read as a pill rather than as a lozenge;
     * full for the rest, since there is nothing to say with a partial one.
     */
    public float fill() {
        return fill;
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
