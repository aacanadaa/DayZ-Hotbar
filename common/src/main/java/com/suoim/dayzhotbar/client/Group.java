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
