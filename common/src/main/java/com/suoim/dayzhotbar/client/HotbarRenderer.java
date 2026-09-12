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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * The DayZ-style hotbar: nine slots plus the offhand on a single flat panel.
 * <p>
 * The geometry follows DayZ Inventory's slots - an inner wash on a flat panel and
 * no outlines - so the two mods look like the same piece of software. Slot state is
 * carried by the colour of that wash alone, never by a border or an underline.
 */
public final class HotbarRenderer {
    private HotbarRenderer() {}

    /**
     * Cell pitch. The grey box fills all but one pixel of it, so the boxes are as
     * large as they can be while still being separable.
     */
    private static final int PITCH = 23;
    /** The grey box inside a cell. One pixel of cell is left between boxes. */
    private static final int BOX = PITCH - 1;
    /** Gap separating the offhand slot from the nine. */
    private static final int SEPARATOR = 8;
    /**
     * Distance from the bottom of the screen.
     * <p>
     * Larger than the status readout's own bottom margin on purpose. Both rows are
     * anchored by their bottom edge, but a box is 22px tall against an icon's 15, and
     * the readout reserves 8px under its icons for the trend markers - so at equal
     * margins the icons sit in the top of the boxes' band and read as floating above
     * the hotbar. Four extra pixels centres the boxes on the icons.
     */
    private static final int MARGIN = 14;

    /** Alpha of the held slot's coloured wash. The colour alone marks the held slot. */
    private static final int ACTIVE_WASH_ALPHA = 0x66;

    /**
     * The x of the hotbar's left edge. Exposed so other elements can anchor to the bar
     * rather than to a screen corner.
     */
    public static int leftEdge(int screenWidth) {
        int totalWidth = 9 * PITCH + SEPARATOR + PITCH;
        return (screenWidth - totalWidth) / 2;
    }

    /** The y of the hotbar's bottom edge, which is one pixel inside its last cell. */
    public static int bottomEdge(int screenHeight) {
        return screenHeight - MARGIN - 1;
    }

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

        // No panel behind the row. The boxes are the hotbar; a backing plate around
        // them only added a dark border the boxes already define for themselves.
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getItem(i);
            boolean active = i == selected;
            drawSlot(graphics, minecraft, left + i * PITCH, top, stack,
                    active ? activeColor(player, stack, swapProgress) : 0,
                    stack.isEmpty());
        }

        int offhandX = left + hotbarWidth + SEPARATOR;
        ItemStack offhand = player.getOffhandItem();
        drawSlot(graphics, minecraft, offhandX, top, offhand, 0, offhand.isEmpty());

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
        graphics.fill(x, y, x + BOX, y + BOX, wash);

        if (!stack.isEmpty()) {
            int itemX = x + (BOX - 16) / 2;
            int itemY = y + (BOX - 16) / 2;
            graphics.renderItem(stack, itemX, itemY);
            graphics.renderItemDecorations(minecraft.font, stack, itemX, itemY);
        }
    }

    // No attack-strength indicator is drawn. One used to sit to the right of the
    // offhand slot, replacing the one vanilla draws inside its hotbar, which
    // cancelling renderHotbar removes. It was taken out on request. Vanilla's own can
    // still be had by setting Options > Video Settings > Attack Indicator to
    // "Crosshair" - only the hotbar position is lost with the vanilla hotbar.
}
