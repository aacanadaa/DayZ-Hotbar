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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The DayZ-style hotbar: nine slots plus the offhand, drawn as individual flat
 * panels rather than one long bar, so it matches the DayZ Inventory screen.
 * <p>
 * Slot outline colour is the whole point of this class - see {@link SlotState}.
 */
public final class HotbarRenderer {
    private HotbarRenderer() {}

    /** Slot edge length. Slightly larger than vanilla's 20 so items breathe. */
    private static final int SLOT = 20;
    /** Gap between adjacent hotbar slots. */
    private static final int GAP = 2;
    /** Wider gap separating the offhand slot from the nine. */
    private static final int OFFHAND_GAP = 10;
    /** Distance from the bottom of the screen. */
    private static final int MARGIN = 4;
    /** Inset of the item icon inside its slot. */
    private static final int ITEM_INSET = 2;

    /** Width of the attack-strength bar drawn beside the hotbar. */
    private static final int ATTACK_W = 4;
    private static final int ATTACK_GAP = 8;

    /**
     * Draws the hotbar.
     *
     * @return false if there is nothing to draw, in which case the caller should
     *         let vanilla render instead rather than cancelling it
     */
    public static boolean render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int selected = inventory.selected;

        int hotbarWidth = 9 * SLOT + 8 * GAP;
        int totalWidth = hotbarWidth + OFFHAND_GAP + SLOT;
        int left = (screenWidth - totalWidth) / 2;
        int top = screenHeight - MARGIN - SLOT;

        for (int i = 0; i < 9; i++) {
            int x = left + i * (SLOT + GAP);
            ItemStack stack = inventory.getItem(i);
            drawSlot(graphics, minecraft, x, top, stack, stateFor(player, stack, i == selected));
        }

        int offhandX = left + hotbarWidth + OFFHAND_GAP;
        ItemStack offhand = player.getOffhandItem();
        drawSlot(graphics, minecraft, offhandX, top, offhand,
                offhand.isEmpty() ? SlotState.EMPTY : SlotState.FILLED);

        drawAttackIndicator(graphics, player, offhandX + SLOT + ATTACK_GAP, top);

        return true;
    }

    /**
     * Selection-driven state. Durability is intentionally not consulted here.
     */
    private static SlotState stateFor(LocalPlayer player, ItemStack stack, boolean selected) {
        if (stack.isEmpty()) {
            return SlotState.EMPTY;
        }
        if (!selected) {
            return SlotState.FILLED;
        }
        // Selected: the item is in hand. If it is on cooldown it cannot actually
        // be used, which is the one case the red state is for.
        return player.getCooldowns().isOnCooldown(stack.getItem()) ? SlotState.BLOCKED : SlotState.ACTIVE;
    }

    private static void drawSlot(GuiGraphics graphics, Minecraft minecraft, int x, int y,
                                 ItemStack stack, SlotState state) {
        // Flat panel, then the faint inner wash every DayZ Inventory slot has.
        HudTheme.panel(graphics, x, y, SLOT, SLOT);
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, HudTheme.SLOT_INNER);

        // The held slot (and the held-but-unusable case) gets a coloured glow as
        // well as an outline, so it is obvious at a glance without reading colour.
        if (state == SlotState.ACTIVE || state == SlotState.BLOCKED) {
            graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, HudTheme.wash(state.color(), 0x40));
        }

        HudTheme.outline(graphics, x, y, SLOT, SLOT, state.color());

        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + ITEM_INSET, y + ITEM_INSET);
            graphics.renderItemDecorations(minecraft.font, stack, x + ITEM_INSET, y + ITEM_INSET);
        }
    }

    /**
     * Cancelling vanilla's hotbar also removes the attack-strength indicator that
     * lives inside it. Losing that would be a real combat regression, so a compact
     * replacement is drawn to the right of the offhand slot: empty when the swing
     * is still charging, full and green when the next hit will be a full one.
     */
    private static void drawAttackIndicator(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        float strength = player.getAttackStrengthScale(0.0F);
        if (strength >= 1.0F) {
            return;
        }

        HudTheme.panel(graphics, x, y, ATTACK_W, SLOT);
        int filled = Math.max(1, Math.round((SLOT - 2) * strength));
        graphics.fill(x + 1, y + SLOT - 1 - filled, x + ATTACK_W - 1, y + SLOT - 1,
                HudTheme.wash(HudTheme.STATE_ACTIVE, 0xB0));
    }
}
