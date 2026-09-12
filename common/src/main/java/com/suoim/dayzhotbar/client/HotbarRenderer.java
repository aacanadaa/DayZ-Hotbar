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
 * The DayZ-style hotbar: nine slots plus the offhand on a single flat panel.
 * <p>
 * The geometry is DayZ Inventory's own slot geometry - an 18px pitch with a
 * 16x16 inner wash and no outline - so the two mods look like the same piece of
 * software. Slot state is carried by the colour of that wash and by an underline
 * beneath the held slot, never by a border.
 */
public final class HotbarRenderer {
    private HotbarRenderer() {}

    /** Cell pitch. The wash inside it is 16x16, matching DayZ Inventory's slots. */
    private static final int PITCH = 18;
    /** Gap separating the offhand slot from the nine. */
    private static final int SEPARATOR = 8;
    /** Padding between the slots and the panel edge. */
    private static final int PAD = 4;
    /** Distance from the bottom of the screen. */
    private static final int MARGIN = 4;

    /** Alpha of the held slot's coloured wash. Kept low - the underline carries it. */
    private static final int ACTIVE_WASH_ALPHA = 0x44;
    /** Thickness of the underline marking the held slot. */
    private static final int UNDERLINE = 2;

    private static final int ATTACK_W = 4;
    private static final int ATTACK_GAP = 6;

    /**
     * Draws the hotbar.
     *
     * @param swapProgress 0 the instant the held slot changes, 1 once the swap has
     *                     settled; drives the yellow-to-green transition
     * @return false if there is nothing to draw, in which case the caller should
     *         let vanilla render instead rather than cancelling it
     */
    public static boolean render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight,
                                 float swapProgress) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        int selected = inventory.selected;

        int hotbarWidth = 9 * PITCH;
        int totalWidth = hotbarWidth + SEPARATOR + PITCH;
        int left = (screenWidth - totalWidth) / 2;
        int top = screenHeight - MARGIN - PITCH;

        // One panel behind the whole row, as every DayZ Inventory element sits on a
        // section panel rather than floating free over the world.
        HudTheme.panel(graphics, left - PAD, top - PAD, totalWidth + PAD * 2, PITCH + PAD * 2);

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            boolean active = i == selected;
            drawSlot(graphics, minecraft, left + i * PITCH, top, stack,
                    active ? activeColor(player, stack, swapProgress) : 0,
                    stack.isEmpty());
        }

        int separatorX = left + hotbarWidth + SEPARATOR / 2;
        graphics.fill(separatorX, top, separatorX + 1, top + PITCH, HudTheme.HAIRLINE);

        int offhandX = left + hotbarWidth + SEPARATOR;
        ItemStack offhand = player.getOffhandItem();
        drawSlot(graphics, minecraft, offhandX, top, offhand, 0, offhand.isEmpty());

        drawAttackIndicator(graphics, player, offhandX + PITCH + ATTACK_GAP, top);
        return true;
    }

    /**
     * The colour of the held slot.
     * <p>
     * It starts yellow the moment the slot changes and resolves to green as the
     * swap settles, so a swap reads as an event rather than an instant flip.
     */
    private static int activeColor(LocalPlayer player, ItemStack stack, float swapProgress) {
        if (!stack.isEmpty() && player.getCooldowns().isOnCooldown(stack.getItem())) {
            return HudTheme.STATE_BLOCKED;
        }
        return HudTheme.lerp(HudTheme.STATE_SWAPPING, HudTheme.STATE_ACTIVE, swapProgress);
    }

    /**
     * @param activeColor the held slot's state colour, or 0 when this slot is not held
     * @param empty       whether the slot is empty, which softens its wash
     */
    private static void drawSlot(GuiGraphics graphics, Minecraft minecraft, int x, int y,
                                 ItemStack stack, int activeColor, boolean empty) {
        int wash = activeColor != 0
                ? HudTheme.wash(activeColor, ACTIVE_WASH_ALPHA)
                : (empty ? HudTheme.SLOT_INNER : HudTheme.SLOT_FILLED);
        graphics.fill(x + 1, y + 1, x + PITCH - 1, y + PITCH - 1, wash);

        // The held slot is marked by an underline in its state colour. It sits in
        // the panel padding below the slot so it never crosses the item.
        if (activeColor != 0) {
            graphics.fill(x + 1, y + PITCH, x + PITCH - 1, y + PITCH + UNDERLINE, activeColor);
        }

        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(minecraft.font, stack, x + 1, y + 1);
        }
    }

    /**
     * Cancelling vanilla's hotbar also removes the attack-strength indicator that
     * lives inside it. Losing that would be a real combat regression, so a compact
     * replacement is drawn to the right of the offhand slot: empty while the swing
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

        HudTheme.panel(graphics, x, y, ATTACK_W, PITCH);
        int filled = Math.max(1, Math.round((PITCH - 2) * strength));
        graphics.fill(x + 1, y + PITCH - 1 - filled, x + ATTACK_W - 1, y + PITCH - 1,
                HudTheme.wash(HudTheme.STATE_ACTIVE, 0xB0));
    }
}
