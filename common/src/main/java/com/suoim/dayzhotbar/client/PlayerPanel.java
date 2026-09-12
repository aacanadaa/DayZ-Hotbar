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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The readout to the left of the hotbar: what is in hand, and what is on your back.
 * <p>
 * Two panels stacked. The upper one is the held item - its condition as a coloured dot
 * and its name beside it. The lower one is a row of stance, armour, and an armour bar.
 * <p>
 * Both are drawn on the same flat panel the rest of the HUD uses, and the stack is
 * anchored to the hotbar rather than to the screen: its right edge sits a fixed gap
 * from the hotbar's left edge and its bottom lines up with the hotbar's. So the whole
 * cluster moves together if the hotbar ever moves, and nothing is pinned to a corner
 * where it would drift apart from the bar at a different window size.
 */
public final class PlayerPanel {
    private PlayerPanel() {}

    /** Panel width. Also the length of the armour bar plus its icons. */
    private static final int WIDTH = 132;
    /** Held-item panel height: one line of text with room to breathe. */
    private static final int ITEM_H = 18;
    /** Space between the two panels. */
    private static final int GAP = 3;
    /** Lower panel height, sized to the stance and shield marks. */
    private static final int BAR_H = 20;
    /** Inset from a panel's edge to its contents. */
    private static final int PAD = 4;
    /** Space between this stack and the hotbar it is anchored to. */
    private static final int HOTBAR_GAP = 10;

    private static final int DOT_RADIUS = 2;
    /** Marks in the lower panel are drawn at one screen pixel per cell. */
    private static final int MARK = Icons.GRID;
    private static final int ARMOR_BAR_H = 5;

    /** Armour runs to 20, the same scale as the vanilla bar. */
    private static final float MAX_ARMOR = 20.0F;

    // DayZ grades an item by how much durability is left. These are the floors of
    // each grade, as fractions.
    private static final float PRISTINE_AT = 0.90F;
    private static final float WORN_AT = 0.75F;
    private static final float DAMAGED_AT = 0.50F;
    private static final float BADLY_DAMAGED_AT = 0.25F;

    /** Draws the whole stack, anchored to the hotbar. */
    public static void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        int left = HotbarRenderer.leftEdge(screenWidth) - HOTBAR_GAP - WIDTH;
        int bottom = HotbarRenderer.bottomEdge(screenHeight);

        int barTop = bottom - BAR_H;
        int itemTop = barTop - GAP - ITEM_H;

        drawHeldItem(graphics, minecraft, left, itemTop, player);
        drawEquipmentBar(graphics, minecraft, left, barTop, player);
    }

    /**
     * The held item: condition dot, then name.
     * <p>
     * The right half is left empty on purpose. That is where a weapon's firing mode
     * and range belong, and where the ammo mark will go once there is a gun mod to
     * read them from - a placeholder there would only have to be taken back out.
     */
    private static void drawHeldItem(GuiGraphics graphics, Minecraft minecraft, int x, int y,
                                     LocalPlayer player) {
        HudTheme.panel(graphics, x, y, WIDTH, ITEM_H);

        ItemStack stack = player.getMainHandItem();
        int dotX = x + PAD + DOT_RADIUS;
        HudTheme.dot(graphics, dotX, y + ITEM_H / 2, DOT_RADIUS,
                stack.isEmpty() ? HudTheme.CONDITION_RUINED : conditionColor(stack));

        String name = stack.isEmpty() ? "Empty" : stack.getHoverName().getString();
        int textX = dotX + DOT_RADIUS + PAD;
        String trimmed = minecraft.font.plainSubstrByWidth(name, x + WIDTH - PAD - textX);
        graphics.drawString(minecraft.font, trimmed, textX, y + (ITEM_H - 8) / 2,
                HudTheme.TEXT_BRIGHT, true);
    }

    /** Stance mark, shield, then the armour bar filling whatever is left. */
    private static void drawEquipmentBar(GuiGraphics graphics, Minecraft minecraft, int x, int y,
                                         LocalPlayer player) {
        HudTheme.panel(graphics, x, y, WIDTH, BAR_H);

        int markY = y + (BAR_H - MARK) / 2;

        Icons.drawSolid(graphics, stanceShape(player), x + PAD, markY, HudTheme.TEXT_BRIGHT);

        int shieldX = x + PAD + MARK + PAD;
        // Outlined rather than solid. Filled, a fifteen-cell shield is a white blob
        // beside a one-pixel stance figure; as a vessel it matches the status icons it
        // sits near.
        Icons.drawVessel(graphics, Icons.SHIELD, shieldX, markY, 1, 1.0F,
                HudTheme.TEXT_BRIGHT, HudTheme.TEXT_BRIGHT);

        int barX = shieldX + MARK + PAD;
        int barWidth = x + WIDTH - PAD - barX;
        int barY = y + (BAR_H - ARMOR_BAR_H) / 2;

        graphics.fill(barX, barY, barX + barWidth, barY + ARMOR_BAR_H, HudTheme.SLOT_INNER);

        int filled = Math.round(barWidth * Math.min(1.0F, player.getArmorValue() / MAX_ARMOR));
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + ARMOR_BAR_H, HudTheme.TIER_NORMAL);
        }
    }

    /**
     * Which of the three stance marks to draw.
     * <p>
     * Crouching wins over sprinting because the two cannot both be true, and a player
     * who is crouched should read as crouched whatever else the game thinks they are
     * doing.
     */
    private static String[] stanceShape(LocalPlayer player) {
        if (player.isCrouching()) {
            return Icons.STANCE_CROUCH;
        }
        if (player.isSprinting()) {
            return Icons.STANCE_RUN;
        }
        return Icons.STANCE_WALK;
    }

    /** The DayZ condition grade of an item, as a colour. */
    private static int conditionColor(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            // Nothing to wear out. Reporting it as pristine is truer than inventing a
            // grade for an item that has no durability at all.
            return HudTheme.CONDITION_PRISTINE;
        }

        float remaining = 1.0F - stack.getDamageValue() / (float) stack.getMaxDamage();
        if (remaining >= PRISTINE_AT) {
            return HudTheme.CONDITION_PRISTINE;
        }
        if (remaining >= WORN_AT) {
            return HudTheme.CONDITION_WORN;
        }
        if (remaining >= DAMAGED_AT) {
            return HudTheme.CONDITION_DAMAGED;
        }
        if (remaining >= BADLY_DAMAGED_AT) {
            return HudTheme.CONDITION_BADLY_DAMAGED;
        }
        return HudTheme.CONDITION_RUINED;
    }
}
