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
 * What a hotbar slot's outline says about it.
 * <p>
 * These are driven by selection and usability only. Durability is deliberately
 * not part of this yet - it is a separate feature for later.
 */
public enum SlotState {
    /** Nothing in the slot. */
    EMPTY(HudTheme.STATE_EMPTY),
    /** Holds an item, but is not the held slot. */
    FILLED(HudTheme.STATE_FILLED),
    /** The slot currently in hand. */
    ACTIVE(HudTheme.STATE_ACTIVE),
    /** The slot in hand, but its item cannot be used right now (e.g. on cooldown). */
    BLOCKED(HudTheme.STATE_BLOCKED);

    private final int color;

    SlotState(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
