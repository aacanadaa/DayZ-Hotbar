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
 * A status shown in the bottom-right readout, with the sprite it draws from and
 * the rate thresholds that decide how many chevrons its trend arrow gets.
 * <p>
 * The thresholds are in each stat's own display units and are measured over one
 * second. They are deliberately low for the stats that move slowly: natural
 * health regeneration is only about 0.25 HP per second, so a threshold of 1.0
 * would never fire and the arrow would never show that you were healing.
 * <p>
 * Order here is draw order, left to right.
 */
public enum Stat {
    /** Health, or the mount's health while riding. */
    HEALTH(true, IconSprites.U_HEART_EMPTY, IconSprites.V_HEART,
            IconSprites.U_HEART_FULL, IconSprites.V_HEART, 0.05F, 3.0F),

    /** Food level. Saturation is drawn over it as a secondary fill. */
    FOOD(true, IconSprites.U_FOOD_EMPTY, IconSprites.V_FOOD,
            IconSprites.U_FOOD_FULL, IconSprites.V_FOOD, 0.05F, 4.0F),

    /** Armour points. Hidden when the player has none. */
    ARMOR(true, IconSprites.U_ARMOR_EMPTY, IconSprites.V_ARMOR,
            IconSprites.U_ARMOR_FULL, IconSprites.V_ARMOR, 0.05F, 4.0F),

    /** Air. Hidden while the player is not underwater. */
    AIR(true, IconSprites.U_AIR_EMPTY, IconSprites.V_AIR,
            IconSprites.U_AIR_FULL, IconSprites.V_AIR, 0.5F, 50.0F),

    /** Golden absorption hearts. Hidden when there are none. */
    ABSORPTION(true, IconSprites.U_HEART_EMPTY, IconSprites.V_HEART,
            IconSprites.U_ABSORB_FULL, IconSprites.V_HEART, 0.05F, 3.0F),

    /**
     * Experience. Drawn procedurally rather than from the sheet, because the
     * bar has to carry the level number alongside it.
     */
    XP(false, 0, 0, 0, 0, 5.0F, 50.0F);

    private final boolean sprite;
    private final int uEmpty;
    private final int vEmpty;
    private final int uFull;
    private final int vFull;
    private final float minor;
    private final float major;

    Stat(boolean sprite, int uEmpty, int vEmpty, int uFull, int vFull, float minor, float major) {
        this.sprite = sprite;
        this.uEmpty = uEmpty;
        this.vEmpty = vEmpty;
        this.uFull = uFull;
        this.vFull = vFull;
        this.minor = minor;
        this.major = major;
    }

    /** False for stats drawn with geometry instead of a sheet sprite. */
    public boolean usesSprite() {
        return sprite;
    }

    public int uEmpty() {
        return uEmpty;
    }

    public int vEmpty() {
        return vEmpty;
    }

    public int uFull() {
        return uFull;
    }

    public int vFull() {
        return vFull;
    }

    /** Delta over one second at or above this shows one chevron. */
    public float minor() {
        return minor;
    }

    /** Delta over one second at or above this shows two chevrons. */
    public float major() {
        return major;
    }
}
