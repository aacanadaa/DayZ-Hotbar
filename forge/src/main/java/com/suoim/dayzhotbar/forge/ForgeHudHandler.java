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
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the DayZ HUD on Forge, and suppresses the vanilla elements it replaces.
 * <p>
 * This is deliberately <em>not</em> the mechanism Fabric uses. On Fabric the mod
 * mixes into {@code Gui} and cancels {@code renderItemHotbar},
 * {@code renderPlayerHealth} and friends at their head. Forge cannot work that way,
 * and the reason changed in 1.21.1.
 * <p>
 * In 1.20.1 Forge drew the HUD from {@code ForgeGui extends Gui} through
 * {@code GuiOverlayManager}, and each vanilla element was its own overlay. In 1.21.1
 * that whole system is gone: there is no {@code ForgeGui}, no
 * {@code GuiOverlayManager} and no {@code RenderGuiOverlayEvent}. Vanilla now builds
 * its HUD as a {@code LayeredDraw}, and Forge swaps in its own
 * {@code ForgeLayeredDraw} subclass and names the layers. The supported hook is
 * {@link AddGuiOverlayLayersEvent}, fired from {@code ForgeLayeredDraw.resolveLayers()}
 * at the end of {@code Gui}'s constructor, with the fully built tree.
 * <p>
 * So Forge gets the loader-native equivalent of the mixin: switch the replaced
 * layers off and add the DayZ version in their place. Switching a layer off rather
 * than skipping it keeps the original property that chat, the tab list and the
 * scoreboard still render on top of the HUD - those live in the post-sleep stack,
 * which is drawn after the stack the hotbar is in.
 */
@Mod.EventBusSubscriber(modid = DayZHotbarForge.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeHudHandler {

    /** This mod's single layer. */
    private static final ResourceLocation DAYZ_HUD =
            ResourceLocation.fromNamespaceAndPath(DayZHotbarForge.MOD_ID, "hud");

    private ForgeHudHandler() {
    }

    /**
     * Swaps the vanilla HUD for the DayZ one, once, as the layer tree is resolved.
     * <p>
     * The tree this runs against is
     * {@code VANILLA_ROOT → PRE_SLEEP_STACK → {camera, crosshair, hotbar,
     * experience, effects, boss}} with the sleep, title, chat, tab-list and
     * scoreboard layers as siblings of {@code PRE_SLEEP_STACK}. Both layers this
     * module replaces live in {@code PRE_SLEEP_STACK}, so that is the stack to
     * address; addressing the root would not find them, because they are nested.
     * <p>
     * Two vanilla layers go, not the five you might expect from the other loaders:
     * <ul>
     *   <li>{@code HOTBAR} is {@code renderHotbarAndDecorations}. In 1.21.1 that one
     *       method draws the slot row, the experience bar, the health row, the
     *       mount's health, the mount's jump meter <em>and</em> the selected item's
     *       name. Forge never split them, so the whole block is one layer and there
     *       is nothing finer to switch off.</li>
     *   <li>{@code EXPERIENCE} is the level number, which 1.21.1 moved into a method
     *       of its own.</li>
     * </ul>
     * <b>This is where Forge is visibly coarser than the other two.</b> Because the
     * block is monolithic here, the brief "selected item name" popup and the mount's
     * jump-charge bar go with it. Fabric replaces only {@code renderItemHotbar} and
     * NeoForge cancels {@code HOTBAR}, {@code JUMP_METER} and friends individually,
     * so both of them keep those two elements. Neither loss is worth the alternative
     * - re-drawing vanilla's own popup here would duplicate the held-item panel on
     * the left of the hotbar, which shows the same name permanently.
     */
    @SubscribeEvent
    public static void onAddLayers(AddGuiOverlayLayersEvent event) {
        ForgeLayeredDraw root = event.getLayeredDraw();

        root.addConditionTo(ForgeLayeredDraw.PRE_SLEEP_STACK, ForgeLayeredDraw.HOTBAR, () -> false);
        root.addConditionTo(ForgeLayeredDraw.PRE_SLEEP_STACK, ForgeLayeredDraw.EXPERIENCE, () -> false);

        // Gated on hideGui for the same reason Fabric is: there the HUD's own
        // methods are simply never reached while the HUD is hidden, so F1 has to
        // hide this layer too or the loaders disagree about what F1 does.
        root.addWithCondition(ForgeLayeredDraw.PRE_SLEEP_STACK, DAYZ_HUD, ForgeHudHandler::render,
                () -> !Minecraft.getInstance().options.hideGui);
    }

    /**
     * Draws the whole DayZ HUD.
     * <p>
     * One layer rather than several, because the two halves are anchored to the
     * same band at the bottom of the screen and nothing else draws between them.
     * The trend history is sampled here, ahead of the readout that consumes it -
     * sampling once per frame is what {@code onFrame} keys off, so it has to happen
     * before the first draw of the frame and not between the two.
     */
    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.getGuiTicks(), minecraft);
        DayZHotbarHud.INSTANCE.renderHotbar(graphics, minecraft);
        DayZHotbarHud.INSTANCE.renderStatus(graphics, minecraft);
    }
}
