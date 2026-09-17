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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
import net.minecraft.client.gui.Gui;
//?}
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
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
 * the end of the render would sit on top of them.
 * <p>
 * Injecting into these methods also inherits vanilla's own visibility rules for
 * free - the HUD still disappears behind a screen, in spectator mode and when the
 * player presses F1.
 * <p>
 * <b>This one file spans five API eras</b>, because the HUD was rewritten three
 * times between 1.20.1 and 26.3 and the target class moved on the last rewrite.
 * The era boundaries below were read off the real mapped jars, not inferred:
 * <ul>
 *   <li><b>1.20.1 - 1.20.4</b> - {@code Gui}; {@code render(GuiGraphics, float)},
 *       {@code renderHotbar(float, GuiGraphics)}, and {@code renderPlayerHealth}
 *       draws health, food, armour and air together.</li>
 *   <li><b>1.20.5 - 1.20.6</b> - {@code renderHotbar} becomes
 *       {@code renderItemHotbar(GuiGraphics, float)} and the experience level is
 *       split into its own {@code renderExperienceLevel}. Vanilla moves to a
 *       {@code LayeredDraw}, but {@code render} still takes a {@code float}.</li>
 *   <li><b>1.21 - 1.21.5</b> - the partial tick becomes a {@code DeltaTracker};
 *       from 1.21.2 the food, armour and air rows are called from {@code render}
 *       rather than from {@code renderPlayerHealth}, so they are cancelled
 *       separately.</li>
 *   <li><b>1.21.6 - 1.21.11</b> - the experience bar becomes a "contextual bar"
 *       and the whole bottom block is drawn by one
 *       {@code renderHotbarAndDecorations}, which is cancelled instead.</li>
 *   <li><b>26.1</b> - {@code GuiGraphics} becomes {@code GuiGraphicsExtractor}
 *       and every method is renamed {@code render*} to {@code extract*}.</li>
 *   <li><b>26.2 - 26.3</b> - the HUD moves out of {@code Gui} into a new
 *       {@code Hud} class, which is what this mixin targets from here on.</li>
 * </ul>
 */
//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
@Mixin(Gui.class)
//?}
public class GuiMixin {

    /** Samples the trend history once per tick, before anything is drawn. */
    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void dayzHotbar$sample(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //?} else if >=1.21 {
    @Inject(method = "render", at = @At("HEAD"))
    private void dayzHotbar$sample(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //?} else {
    @Inject(method = "render", at = @At("HEAD"))
    private void dayzHotbar$sample(GuiGraphicsExtractor graphics, float partialTick, CallbackInfo ci) {
    //?}
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.hud.getGuiTicks(), minecraft);
    }

    /**
     * The hotbar, and on the modern line the whole bottom block with it.
     * <p>
     * From 1.21.6 the slot row, the experience contextual bar, the status row and
     * the mount health are all drawn by one {@code renderHotbarAndDecorations}, so
     * that method is cancelled and both halves of the DayZ HUD are drawn here. On
     * the older line the slot row is its own method and the status row is injected
     * separately below.
     */
    //? if >=26.1 {
    @Inject(method = "extractHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.dayzHotbar$drawHotbarAndStatus(graphics, ci);
    }
    //?} else if >=1.21.6 {
    @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        this.dayzHotbar$drawHotbarAndStatus(graphics, ci);
    }
    //?} else if >=1.20.5 {
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(GuiGraphicsExtractor graphics, float partialTick, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderHotbar(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?} else {
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceHotbar(float partialTick, GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderHotbar(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?}

    //? if >=1.21.6 {
    /**
     * Draws both halves of the DayZ HUD and cancels the vanilla block if either was
     * drawn. Both guard on {@code hideGui} and spectator mode, so in those states
     * nothing is drawn and vanilla renders unchanged.
     */
    private void dayzHotbar$drawHotbarAndStatus(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean drew = DayZHotbarHud.INSTANCE.renderHotbar(graphics, minecraft);
        drew |= DayZHotbarHud.INSTANCE.renderStatus(graphics, minecraft);
        if (drew) {
            ci.cancel();
        }
    }
    //?}

    /**
     * The status readout. Up to 1.21.5 this is its own method; from 1.21.6 it is
     * drawn by {@link #dayzHotbar$drawHotbarAndStatus}.
     */
    //? if <1.21.6 {
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$replaceStatus(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (DayZHotbarHud.INSTANCE.renderStatus(graphics, Minecraft.getInstance())) {
            ci.cancel();
        }
    }
    //?}

    /**
     * The experience bar. Drawn by its own method up to 1.21.5; from 1.21.6 it is
     * part of the contextual bar inside {@code renderHotbarAndDecorations}.
     */
    //? if <1.21.6 {
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceBar(GuiGraphicsExtractor graphics, int x, CallbackInfo ci) {
        ci.cancel();
    }
    //?}

    /**
     * The level number on the bar. Split from the bar itself in 1.20.5, and folded
     * back into the bar in 1.21.6 - so it only exists in this range.
     */
    //? if >=1.20.5 && <1.21 {
    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceLevel(GuiGraphicsExtractor graphics, float partialTick, CallbackInfo ci) {
        ci.cancel();
    }
    //?} else if >=1.21 && <1.21.6 {
    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideExperienceLevel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ci.cancel();
    }
    //?}

    /**
     * Mount health. The health icon shows the mount's health while riding, so
     * leaving vanilla's row here would draw the same thing twice. Folded into
     * {@code renderHotbarAndDecorations} from 1.21.6.
     */
    //? if <1.21.6 {
    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideVehicleHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ci.cancel();
    }
    //?}

    /**
     * Food, armour and air. They are drawn from {@code renderPlayerHealth} up to
     * 1.21.1, and are cancelled with it; 1.21.2 moved them to their own methods
     * called directly from {@code render}, so they have to be cancelled here too.
     * From 1.21.6 they are inside {@code renderHotbarAndDecorations} again.
     */
    //? if >=1.21.2 && <1.21.6 {
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideFood(GuiGraphicsExtractor graphics,
                                     net.minecraft.world.entity.player.Player player,
                                     int x, int y, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void dayzHotbar$hideArmor(GuiGraphicsExtractor graphics,
                                             net.minecraft.world.entity.player.Player player,
                                             int x, int y, int width, int height, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "renderAirBubbles", at = @At("HEAD"), cancellable = true)
    private void dayzHotbar$hideAir(GuiGraphicsExtractor graphics,
                                    net.minecraft.world.entity.player.Player player,
                                    int x, int y, int width, CallbackInfo ci) {
        ci.cancel();
    }
    //?}
}
