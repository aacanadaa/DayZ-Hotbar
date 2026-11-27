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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
//? if >=1.21.8 {
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
import net.minecraftforge.fml.common.Mod;

/**
 * Draws the DayZ HUD on Forge, and suppresses the vanilla elements it replaces.
 * <p>
 * This is deliberately <em>not</em> the mechanism Fabric uses. On Fabric the mod
 * mixes into {@code Gui} and cancels {@code renderItemHotbar},
 * {@code renderPlayerHealth} and friends at their head. Forge cannot work that
 * way - there is no {@code ForgeGui} any more and the overlays are gone - so the
 * loader-native equivalent is used: switch the replaced layers off in Forge's own
 * layer tree and add the DayZ version in their place.
 * <p>
 * The layer tree changed shape three times in this matrix, which is why the body
 * is conditional:
 * <ul>
 *   <li><b>1.20.6 - 1.21.5</b> - {@code ForgeLayeredDraw} extends vanilla's
 *       {@code LayeredDraw}. The slot row and the experience bar are separate
 *       layers ({@code HOTBAR}, {@code EXPERIENCE}) nested in
 *       {@code PRE_SLEEP_STACK}.</li>
 *   <li><b>1.21.8 - 1.21.10</b> - the whole bottom block is one layer,
 *       {@code HOTBAR_AND_DECOS}.</li>
 *   <li><b>1.21.11</b> - the block is split again, but into its modern pieces:
 *       {@code ITEM_HOTBAR}, {@code HEALTH_BAR}, {@code VEHICLE_HEALTH},
 *       {@code EXPERIENCE_LEVEL} and {@code CONTEXTUAL_INFO}.</li>
 * </ul>
 * Forge did not ship the layered API at all on 1.21, 1.21.6 or 1.21.7, which is
 * why those versions have no Forge node - see docs/BUILDING.en.md section 7.
 */
@Mod.EventBusSubscriber(modid = DayZHotbarForge.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForgeHudHandler {

    /** This mod's single layer. */
    //? if <1.21 {
    private static final Identifier DAYZ_HUD = new net.minecraft.resources.ResourceLocation(
            DayZHotbarForge.MOD_ID, "hud");
    //?} else {
    private static final Identifier DAYZ_HUD =
            Identifier.fromNamespaceAndPath(DayZHotbarForge.MOD_ID, "hud");
    //?}

    private ForgeHudHandler() {
    }

    /**
     * Swaps the vanilla HUD for the DayZ one, once, as the layer tree is resolved.
     * The replaced layers are switched off and the DayZ version added at the same
     * point in the render order, which keeps chat, the tab list and the scoreboard
     * drawing on top.
     */
    @SubscribeEvent
    public static void onAddLayers(AddGuiOverlayLayersEvent event) {
        ForgeLayeredDraw root = event.getLayeredDraw();

        //? if <1.21.8 {
        root.addConditionTo(ForgeLayeredDraw.PRE_SLEEP_STACK, ForgeLayeredDraw.HOTBAR, () -> false);
        root.addConditionTo(ForgeLayeredDraw.PRE_SLEEP_STACK, ForgeLayeredDraw.EXPERIENCE, () -> false);
        //?} else if <1.21.11 {
        root.addConditionTo(ForgeLayeredDraw.HOTBAR_AND_DECOS, () -> false);
        //?} else {
        root.addConditionTo(ForgeLayeredDraw.ITEM_HOTBAR, () -> false);
        root.addConditionTo(ForgeLayeredDraw.HEALTH_BAR, () -> false);
        root.addConditionTo(ForgeLayeredDraw.VEHICLE_HEALTH, () -> false);
        root.addConditionTo(ForgeLayeredDraw.EXPERIENCE_LEVEL, () -> false);
        root.addConditionTo(ForgeLayeredDraw.CONTEXTUAL_INFO, () -> false);
        //?}

        //? if <1.21.8 {
        // Gated on hideGui for the same reason Fabric is: there the HUD's own
        // methods are simply never reached while the HUD is hidden, so F1 has to
        // hide this layer too or the loaders disagree about what F1 does.
        root.addWithCondition(ForgeLayeredDraw.PRE_SLEEP_STACK, DAYZ_HUD, ForgeHudHandler::render,
                () -> !Minecraft.getInstance().options.hideGui);
        //?} else {
        // The newer ForgeLayeredDraw dropped the condition form from `add`; the
        // render body handles hideGui and spectator mode itself and no-ops there.
        root.add(ForgeLayeredDraw.PRE_SLEEP_STACK, DAYZ_HUD, ForgeHudHandler::render);
        //?}
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
    //? if >=1.21 {
    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    //?} else {
    private static void render(GuiGraphicsExtractor graphics, float partialTick) {
    //?}
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.hud.getGuiTicks(), minecraft);
        DayZHotbarHud.INSTANCE.renderHotbar(graphics, minecraft);
        DayZHotbarHud.INSTANCE.renderStatus(graphics, minecraft);
    }
}
