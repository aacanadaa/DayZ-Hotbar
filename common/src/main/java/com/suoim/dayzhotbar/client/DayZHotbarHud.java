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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * Holds the per-stat trend history and turns the player's current state into the cells
 * the readout draws.
 * <p>
 * Sampling happens once per client tick, not once per frame, because velocities and
 * the hotbar swap animation are both measured in ticks. The tick is detected from the
 * GUI's own tick counter, so no extra game event is needed and a paused or hidden HUD
 * cannot leave a gap in the history that would read as a sudden change.
 */
public final class DayZHotbarHud {
    public static final DayZHotbarHud INSTANCE = new DayZHotbarHud();

    /** Food and health are both out of 20, which is the unit the fill uses. */
    private static final float MAX_FOOD = 20.0F;
    /** Ticks the held slot spends resolving from yellow to green after a swap. */
    private static final int SWAP_TICKS = 6;
    /** Ticks an effect mark takes to fade out once the effect is gone. */
    private static final int EFFECT_FADE_TICKS = 20;

    private final EnumMap<Stat, VelocityTracker> trackers = new EnumMap<>(Stat.class);
    /** Ticks of fade left per effect family. Full means the family is active. */
    private final EnumMap<EffectGroup, Integer> effectFade = new EnumMap<>(EffectGroup.class);

    private int lastGuiTicks = Integer.MIN_VALUE;
    private int lastSelected = Integer.MIN_VALUE;
    private int swapTicks = 0;

    private DayZHotbarHud() {
        for (Stat stat : Stat.values()) {
            trackers.put(stat, new VelocityTracker(stat.minor(), stat.major()));
        }
        for (EffectGroup group : EffectGroup.values()) {
            effectFade.put(group, 0);
        }
    }

    /**
     * Called once per frame from the HUD. Advances the samplers if a game tick has
     * elapsed since the last call.
     */
    public void onFrame(int guiTicks, Minecraft minecraft) {
        if (guiTicks == lastGuiTicks) {
            return;
        }

        // More than a tick or two missing means the HUD was not rendering - a screen
        // was open, or the game was paused. Comparing across that gap would report a
        // large phantom velocity, so start the history over instead.
        boolean gap = lastGuiTicks != Integer.MIN_VALUE && guiTicks - lastGuiTicks > 2;
        lastGuiTicks = guiTicks;

        if (gap) {
            resetTrackers();
        }

        sample(minecraft);
    }

    private void sample(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            resetTrackers();
            lastSelected = Integer.MIN_VALUE;
            return;
        }

        // Tick the hold timers before recording this tick's movement, so a marker
        // raised below survives until the next tick rather than ageing immediately.
        for (VelocityTracker tracker : trackers.values()) {
            tracker.tick();
        }

        FoodData food = player.getFoodData();
        trackers.get(Stat.HEALTH).push(health(player));
        trackers.get(Stat.FOOD).push(food.getFoodLevel());
        trackers.get(Stat.AIR).push(player.getAirSupply());
        trackers.get(Stat.ABSORPTION).push(player.getAbsorptionAmount());

        // Experience is scaled so a level-up (+100) clearly outranks a single orb
        // (+10), which is what makes the level-up read as a two-chevron event.
        trackers.get(Stat.XP).push(player.experienceLevel * 100.0F + player.experienceProgress * 100.0F);

        updateEffectFades(player);
        updateSwap(player);
    }

    /**
     * Collapses the player's active effects to one mark per family, and ages out the
     * families that are no longer active so their marks fade rather than vanish.
     */
    private void updateEffectFades(LocalPlayer player) {
        boolean[] active = new boolean[EffectGroup.values().length];
        for (MobEffectInstance instance : player.getActiveEffects()) {
            active[EffectGroup.of(instance.getEffect()).ordinal()] = true;
        }

        for (EffectGroup group : EffectGroup.values()) {
            if (active[group.ordinal()]) {
                effectFade.put(group, EFFECT_FADE_TICKS);
            } else {
                effectFade.put(group, Math.max(0, effectFade.getOrDefault(group, 0) - 1));
            }
        }
    }

    /**
     * Starts the yellow-to-green transition when the held slot changes. The first
     * sample only seeds the value, so joining a world does not animate a swap that
     * never happened.
     */
    private void updateSwap(LocalPlayer player) {
        int selected = player.getInventory().selected;

        if (lastSelected == Integer.MIN_VALUE) {
            lastSelected = selected;
            swapTicks = 0;
            return;
        }

        if (selected != lastSelected) {
            lastSelected = selected;
            swapTicks = SWAP_TICKS;
        } else if (swapTicks > 0) {
            swapTicks--;
        }
    }

    /** 0 the instant the held slot changes, 1 once the swap has settled. */
    private float swapProgress() {
        if (swapTicks <= 0) {
            return 1.0F;
        }
        return 1.0F - (swapTicks / (float) SWAP_TICKS);
    }

    private void resetTrackers() {
        for (VelocityTracker tracker : trackers.values()) {
            tracker.reset();
        }
    }

    /** Draws the hotbar. Returns false to let vanilla render instead. */
    public boolean renderHotbar(GuiGraphics graphics, Minecraft minecraft) {
        if (minecraft.options.hideGui) {
            return false;
        }
        return HotbarRenderer.render(graphics, minecraft, graphics.guiWidth(), graphics.guiHeight(),
                swapProgress());
    }

    /** Draws the status readout. Returns false to let vanilla render instead. */
    public boolean renderStatus(GuiGraphics graphics, Minecraft minecraft) {
        if (minecraft.options.hideGui) {
            return false;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        StatusRow.render(graphics, minecraft.font, graphics.guiWidth(), graphics.guiHeight(),
                buildCells(player), lastGuiTicks);
        return true;
    }

    /**
     * Builds the readout's cells in draw order: effects on the left, then stats grouped
     * as they appear in {@link Group}. Effects come first because their section is the
     * leftmost, and a section with nothing in it simply contributes no cells - which is
     * what makes its divider appear and disappear with it.
     */
    private List<StatusRow.Cell> buildCells(LocalPlayer player) {
        List<StatusRow.Cell> cells = new ArrayList<>();

        for (EffectGroup group : EffectGroup.values()) {
            int remaining = effectFade.getOrDefault(group, 0);
            if (remaining <= 0) {
                continue;
            }
            cells.add(new StatusRow.Cell(group.shape(), HudTheme.TEXT_BRIGHT,
                    group.fill(), 0.0F, true, group.solid(), 0, false, 0.0F,
                    StatusRow.NO_LEVEL, false, Group.EFFECTS,
                    remaining / (float) EFFECT_FADE_TICKS));
        }

        // While riding something with health, vanilla shows the mount's health instead
        // of yours. Matching that keeps the two readings consistent.
        float maxHealth = maxHealth(player);
        if (maxHealth <= 0.0F) {
            maxHealth = 20.0F;
        }

        FoodData food = player.getFoodData();
        float foodFraction = clamp(food.getFoodLevel() / MAX_FOOD);
        // Saturation can never exceed the food level, so cap it to keep the overlay
        // from ever sitting higher than the fill it rides on.
        float saturationFraction = Math.min(foodFraction, clamp(food.getSaturationLevel() / MAX_FOOD));
        int air = player.getAirSupply();
        int maxAir = Math.max(1, player.getMaxAirSupply());

        // Declaration order decides the order within a section, so this only has to
        // put the sections themselves in order.
        List<Stat> stats = new ArrayList<>(List.of(Stat.values()));
        stats.sort(Comparator.comparingInt(stat -> stat.group().ordinal()));

        for (Stat stat : stats) {
            StatusRow.Cell cell = switch (stat) {
                case HEALTH -> statCell(stat, clamp(health(player) / maxHealth), 0.0F, StatusRow.NO_LEVEL);
                case FOOD -> statCell(stat, foodFraction, saturationFraction, StatusRow.NO_LEVEL);
                // Only while submerged, exactly like vanilla's bubbles.
                case AIR -> air < maxAir
                        ? statCell(stat, clamp(air / (float) maxAir), 0.0F, StatusRow.NO_LEVEL)
                        : null;
                case ABSORPTION -> {
                    float absorption = player.getAbsorptionAmount();
                    yield absorption > 0.0F
                            ? statCell(stat, clamp(absorption / maxHealth), 0.0F, StatusRow.NO_LEVEL)
                            : null;
                }
                case XP -> statCell(stat, clamp(player.experienceProgress), 0.0F,
                        player.experienceLevel);
            };
            if (cell != null) {
                cells.add(cell);
            }
        }

        return cells;
    }

    private StatusRow.Cell statCell(Stat stat, float fraction, float saturation, int level) {
        VelocityTracker tracker = trackers.get(stat);
        return new StatusRow.Cell(stat.shape(), stat.colorFor(fraction, lastGuiTicks),
                fraction, saturation, false, false,
                tracker.chevrons(), tracker.up(), tracker.alpha(),
                level, stat == Stat.ABSORPTION, stat.group(), 1.0F);
    }

    /** The player's own health, or their mount's while riding something alive. */
    private static float health(LocalPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity living && living.isAlive()) {
            return living.getHealth();
        }
        return player.getHealth();
    }

    private static float maxHealth(LocalPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity living && living.isAlive()) {
            return living.getMaxHealth();
        }
        return player.getMaxHealth();
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
