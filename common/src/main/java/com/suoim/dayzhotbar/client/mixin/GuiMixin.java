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
package com.suoim.dayzhotbar.client.mixin;

import com.suoim.dayzhotbar.client.DayZHotbarHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla HUD elements this mod redraws.
 * <p>
 * The injections cancel each element and draw the replacement at that exact point
 * in the render order, rather than drawing everything afterwards. That matters:
 * vanilla renders chat, the tab list and the scoreboard late, and a HUD drawn at
 * the end of {@code render} would sit on top of them.
 * <p>
 * Injecting into these methods also inherits vanilla's own visibility rules for
 * free - the HUD still disappears behind a screen, in spectator mode, and when
 * the player presses F1.
 * <p>
 * Note the argument order on {@code renderHotbar}: in 1.20.1 the partial tick
 * comes <em>before</em> the graphics object, which is the reverse of most of the
 * other render methods and an easy thing to get wrong.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /** Samples the trend history once per tick, before anything is drawn. */
    @Inject(method = "render", at = @At("HEAD"))
    private void dayzHotbar$sample(GuiGraphics graphics, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.getGuiTicks(), minecraft);
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(float partialTick, GuiGraphics graphics, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderHotbar(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    /**
     * Health, food, armour and air are all drawn by this one method in 1.20.1 -
     * there is no separate hook for each - so the whole lot is replaced together.
     */
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceStatus(GuiGraphics graphics, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderStatus(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    /** The experience bar and its level number, replaced by the XP icon instead. */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * Mount health. The health icon shows the mount's health while riding, so
     * leaving vanilla's row here would draw the same thing twice.
     */
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideVehicleHealth(GuiGraphics graphics, CallbackInfo ci) {
        ci.cancel();
    }
}
