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
 * The sections the status readout is divided into, left to right.
 * <p>
 * A line is drawn between adjacent sections that both have something in them. Because
 * a section is only laid out when it is not empty, that falls out of the ordering: the
 * divider before effects appears only when there is an effect to show, while the one
 * between sustenance and vitals is always there.
 * <p>
 * Declaration order is draw order.
 */
public enum Group {
    /** Active potion effects, collapsed to one mark per family. Leftmost. */
    EFFECTS,

    /** Food, and later water and air. The middle. */
    SUSTENANCE,

    /** Health and experience, and later blood. Rightmost, where the eye goes first. */
    VITALS
}
