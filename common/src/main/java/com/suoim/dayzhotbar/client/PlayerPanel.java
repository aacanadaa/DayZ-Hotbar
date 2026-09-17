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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * The readout to the left of the hotbar: what is in hand, and what is on your back.
 * <p>
 * Two panels stacked. The upper one is the held item - its condition as a coloured dot
 * and its name beside it. The lower one is a row of stance, armour, and an armour bar.
 * <p>
 * Both sit on the same wash the hotbar's slots use, not the darker section panel: the
 * hotbar has no backing plate, so a dark card beside it reads as a different material.
 * The stack occupies the bottom-left corner of the screen, with its bottom edge on the
 * hotbar's line so the two read as one row across the width of the HUD.
 */
public final class PlayerPanel {
    private PlayerPanel() {}

    /** Panel width. Also the length of the armour bar plus its marks. */
    private static final int WIDTH = 132;
    /** Held-item panel height: one line of text with room to breathe. */
    private static final int ITEM_H = 14;
    /** Space between the two panels. */
    private static final int GAP = 2;
    /** Lower panel height, sized to the stance and shield marks. */
    private static final int BAR_H = 14;
    /** Inset from a panel's edge to its contents. */
    private static final int PAD = 4;
    /**
     * Distance from the left screen edge. Matches the right margin the status readout
     * uses, so the HUD is inset the same amount at both ends.
     */
    private static final int LEFT_MARGIN = 16;

    private static final int DOT_RADIUS = 2;
    /** Marks in the lower panel are eleven cells, drawn a screen pixel per cell. */
    private static final int MARK = Icons.SHIELD_MARK.length;
    private static final int ARMOR_BAR_H = 4;
    /** The bar's own track, so it reads as a trough rather than a bright stripe. */
    private static final int ARMOR_TRACK = 0x18FFFFFF;
    private static final int ARMOR_FILL = 0xCCEDEDED;

    /** Armour runs to 20, the same scale as the vanilla bar. */
    private static final float MAX_ARMOR = 20.0F;

    // DayZ grades an item by how much durability is left. These are the floors of
    // each grade, as fractions.
    private static final float PRISTINE_AT = 0.90F;
    private static final float WORN_AT = 0.75F;
    private static final float DAMAGED_AT = 0.50F;
    private static final float BADLY_DAMAGED_AT = 0.25F;

    /** Draws the whole stack in the bottom-left corner. */
    public static void render(GuiGraphicsExtractor graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        int left = LEFT_MARGIN;
        // Bottom edge on the hotbar's line, so the HUD reads as one row even though the
        // panel itself hugs the left edge.
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
    private static void drawHeldItem(GuiGraphicsExtractor graphics, Minecraft minecraft, int x, int y,
                                     LocalPlayer player) {
        HudTheme.card(graphics, x, y, WIDTH, ITEM_H);

        ItemStack stack = player.getMainHandItem();
        int dotX = x + PAD + DOT_RADIUS;
        HudTheme.dot(graphics, dotX, y + ITEM_H / 2, DOT_RADIUS,
                stack.isEmpty() ? HudTheme.CONDITION_RUINED : conditionColor(stack));

        String name = stack.isEmpty() ? "Empty" : stack.getHoverName().getString();
        int textX = dotX + DOT_RADIUS + PAD;
        String trimmed = minecraft.font.plainSubstrByWidth(name, x + WIDTH - PAD - textX);
        graphics.text(minecraft.font, trimmed, textX, y + (ITEM_H - 8) / 2,
                HudTheme.TEXT_BRIGHT, true);
    }

    /** Stance mark, shield, then the armour bar filling whatever is left. */
    private static void drawEquipmentBar(GuiGraphicsExtractor graphics, Minecraft minecraft, int x, int y,
                                         LocalPlayer player) {
        HudTheme.card(graphics, x, y, WIDTH, BAR_H);

        int markY = y + (BAR_H - MARK) / 2;

        Icons.drawSolid(graphics, stanceShape(player), x + PAD, markY, HudTheme.TEXT_BRIGHT);

        int shieldX = x + PAD + MARK + PAD;
        // Outlined rather than solid. Filled, an eleven-cell shield is a white blob
        // beside the thin stance figure; as a vessel it matches the status icons.
        Icons.drawVessel(graphics, Icons.SHIELD_MARK, shieldX, markY, 1, 1.0F,
                HudTheme.TEXT_BRIGHT, HudTheme.TEXT_BRIGHT);

        int barX = shieldX + MARK + PAD;
        int barWidth = x + WIDTH - PAD - barX;
        int barY = y + (BAR_H - ARMOR_BAR_H) / 2;

        graphics.fill(barX, barY, barX + barWidth, barY + ARMOR_BAR_H, ARMOR_TRACK);

        int filled = Math.round(barWidth * Math.min(1.0F, player.getArmorValue() / MAX_ARMOR));
        if (filled > 0) {
            graphics.fill(barX, barY, barX + filled, barY + ARMOR_BAR_H, ARMOR_FILL);
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
