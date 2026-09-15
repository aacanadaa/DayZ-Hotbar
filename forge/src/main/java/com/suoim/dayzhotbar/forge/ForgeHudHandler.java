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
package com.suoim.dayzhotbar.forge;

import com.suoim.dayzhotbar.client.DayZHotbarHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/**
 * Drives the DayZ HUD on Forge, and suppresses the vanilla elements it replaces.
 * <p>
 * This is deliberately <em>not</em> the same mechanism Fabric uses. On Fabric the
 * mod mixes into {@code Gui} and cancels {@code renderHotbar}, {@code
 * renderPlayerHealth} and friends at their head. Forge cannot work that way:
 * {@code Minecraft} instantiates {@code ForgeGui extends Gui}, whose {@code render}
 * never calls {@code Gui.render} or any of its internals. It fires
 * {@code RenderGuiEvent} and then walks {@code GuiOverlayManager.getOverlays()},
 * drawing every vanilla element from a separate overlay that dispatches to
 * {@code ForgeGui}'s <em>own</em> methods. Only {@code renderHotbar} survives that
 * trip, because it is public and {@code ForgeGui} does not override it;
 * {@code renderPlayerHealth} and {@code renderVehicleHealth} are private in
 * {@code Gui}, so Forge reimplements them as {@code renderHealth} /
 * {@code renderFood} / {@code renderArmor} / {@code renderAir} and the mixin
 * simply never runs. See CLAUDE.md.
 * <p>
 * So Forge gets the loader-native equivalent: cancel the overlay, draw the
 * replacement at that exact point in the render order. Cancelling an overlay
 * rather than drawing everything afterwards keeps the original property that
 * chat, the tab list and the scoreboard still render on top of the HUD.
 */
@Mod.EventBusSubscriber(modid = DayZHotbarForge.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeHudHandler {

    /**
     * Vanilla's armour, food, air and mount-health rows are all folded into the one
     * DayZ status readout. They are cancelled wherever they appear; the readout
     * itself is drawn once, from {@code PLAYER_HEALTH} below.
     */
    private static final Set<ResourceLocation> REPLACED_BY_STATUS_ROW = Set.of(
            VanillaGuiOverlay.ARMOR_LEVEL.id(),
            VanillaGuiOverlay.FOOD_LEVEL.id(),
            VanillaGuiOverlay.AIR_LEVEL.id(),
            VanillaGuiOverlay.MOUNT_HEALTH.id()
    );

    private ForgeHudHandler() {
    }

    /**
     * Samples the trend history once per frame, before any overlay is drawn - the
     * Forge counterpart of the Fabric mixin's injection at the head of
     * {@code Gui.render}.
     * <p>
     * {@code RenderGuiEvent.Pre} is fired first thing in {@code ForgeGui.render},
     * ahead of the overlay loop, so the sample always lands before the readout that
     * consumes it.
     */
    @SubscribeEvent
    public static void onRenderPre(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.getGuiTicks(), minecraft);
    }

    /**
     * Replaces the vanilla HUD elements with their DayZ equivalents.
     * <p>
     * The overlays are cancelled unconditionally rather than only when the mod drew
     * something. {@code ForgeGui.render} does not consult {@code hideGui} the way
     * {@code Gui.render} does - it fires this event for every registered overlay in
     * every game mode - so cancelling only on a successful draw would leave vanilla's
     * health and food rows visible with F1 pressed. Cancelling always and drawing
     * only when the mod's own {@code hideGui} / null-player guards allow it gives the
     * same result as Fabric, where {@code Gui.render} never reaches these methods at
     * all while the HUD is hidden.
     */
    @SubscribeEvent
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics graphics = event.getGuiGraphics();
        ResourceLocation id = event.getOverlay().id();

        if (VanillaGuiOverlay.HOTBAR.id().equals(id)) {
            DayZHotbarHud.INSTANCE.renderHotbar(graphics, minecraft);
            event.setCanceled(true);
        } else if (VanillaGuiOverlay.PLAYER_HEALTH.id().equals(id)) {
            // The readout is drawn here rather than in the game-mode-gated survival
            // overlays, because PLAYER_HEALTH is the one that is always registered
            // and always fires. It replaces the whole row, so where in the row it is
            // anchored does not matter - only that something draws it exactly once.
            DayZHotbarHud.INSTANCE.renderStatus(graphics, minecraft);
            event.setCanceled(true);
        } else if (VanillaGuiOverlay.EXPERIENCE_BAR.id().equals(id)
                || REPLACED_BY_STATUS_ROW.contains(id)) {
            event.setCanceled(true);
        }
    }
}
