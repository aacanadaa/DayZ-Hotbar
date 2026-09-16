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
package com.suoim.dayzhotbar.client.mixin;

import com.suoim.dayzhotbar.client.DayZHotbarHud;
import net.minecraft.client.DeltaTracker;
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
 * <b>1.21.1 notes.</b> All of these targets were read off the real mapped 1.21.1
 * jar rather than carried over from 1.20.1, and three things moved:
 * <ul>
 *   <li>the partial tick is gone from {@code render} and {@code renderItemHotbar};
 *       both now take a {@link DeltaTracker} instead;</li>
 *   <li>{@code renderHotbar} no longer exists - the hotbar proper is
 *       {@code renderItemHotbar}, and the argument order is now the ordinary
 *       graphics-then-time one rather than 1.20.1's reversed pair;</li>
 *   <li>the experience level is drawn by its own {@code renderExperienceLevel}
 *       method, so hiding the bar alone leaves the number floating.</li>
 * </ul>
 * {@code renderPlayerHealth} is unchanged and still draws health, food, armour
 * and air together, even though 1.21.1 wraps it in a layered renderer - the
 * layer is a thin wrapper over this same method.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /** Samples the trend history once per tick, before anything is drawn. */
    @Inject(method = "render", at = @At("HEAD"))
    private void dayzHotbar$sample(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.getGuiTicks(), minecraft);
    }

    /**
     * The hotbar itself. Only the nine slots and the offhand are replaced - the
     * selected item's name is a separate method and still draws, which is what
     * vanilla does today.
     */
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderHotbar(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    /**
     * Health, food, armour and air are all drawn by this one method in 1.21.1 as
     * well - there is no separate hook for each - so the whole lot is replaced
     * together.
     */
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceStatus(GuiGraphics graphics, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderStatus(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }

    /** The experience bar, replaced by the XP icon instead. */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceBar(GuiGraphics graphics, int x, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * The level number on the bar. A separate method from the bar itself since
     * 1.21.1, and the readout draws the level on the XP icon, so leaving this one
     * alone would print it twice.
     */
    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceLevel(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
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
