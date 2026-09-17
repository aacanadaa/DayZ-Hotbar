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
package com.suoim.dayzhotbar.neoforge;

import com.suoim.dayzhotbar.client.DayZHotbarHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Set;

/**
 * Drives the DayZ HUD on NeoForge, and suppresses the vanilla elements it
 * replaces.
 * <p>
 * Neither of the other two loaders' mechanisms works here. The shared
 * {@code GuiMixin} is compiled against Loom's intermediary refmap and would not
 * resolve, and Forge's {@code AddGuiOverlayLayersEvent} does not exist - NeoForge
 * forked before it and built its own layer registry instead.
 * <p>
 * NeoForge's hook is the better of the two, because it fires per <em>layer</em> at
 * render time rather than once at registration. That means the suppression is
 * conditional on what is actually being drawn, exactly like the Fabric mixin, and
 * the replacement can be drawn at the layer's own point in the render order rather
 * than at a position picked in advance.
 * <p>
 * NeoForge names every vanilla layer, and it split the status row up: where vanilla
 * draws health, armour, food and air (and, from 1.21.2, air separately) NeoForge
 * registers a layer for each. So the readout takes the {@code PLAYER_HEALTH} layer
 * as its anchor and the other three are cancelled alongside it. The experience bar
 * moved into a shared "contextual bar" in 1.21.6, which is what the conditional
 * layer set below follows.
 */
// No `bus` attribute: on NeoForge it defaults to the game bus, and naming it
// explicitly is deprecated for removal.
@EventBusSubscriber(modid = DayZHotbarNeoForge.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeHudHandler {

    /**
     * The experience bar, in whatever form this Minecraft version draws it.
     * <p>
     * Up to 1.21.5 it is a layer of its own, {@code EXPERIENCE_BAR}. 1.21.6 folded
     * the bar and the mount's jump charge into a shared "contextual bar" and
     * renamed the layers with it. A name no layer carries simply never comes up, so
     * the right set for the version is selected at compile time.
     */
    //? if <1.21.6 {
    private static final Set<Identifier> EXPERIENCE_LAYERS = Set.of(
            VanillaGuiLayers.EXPERIENCE_BAR
    );
    //?} else {
    private static final Set<Identifier> EXPERIENCE_LAYERS = Set.of(
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR,
            VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND
    );
    //?}

    /**
     * The rest of the status row. NeoForge registers each of these separately, and
     * all of them are folded into the single DayZ readout.
     */
    private static final Set<Identifier> REPLACED_BY_STATUS_ROW = Set.of(
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH
    );

    private NeoForgeHudHandler() {
    }

    /**
     * Samples the trend history once per frame, before any layer is drawn - the
     * NeoForge counterpart of the Fabric mixin's injection at the head of
     * {@code Gui.render}.
     * <p>
     * It has to be this event rather than the first layer: the readout is drawn from
     * the {@code PLAYER_HEALTH} layer and reads the sample, so a sample taken any
     * later would be one frame stale.
     */
    @SubscribeEvent
    public static void onRenderPre(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        DayZHotbarHud.INSTANCE.onFrame(minecraft.gui.hud.getGuiTicks(), minecraft);
    }

    /**
     * Replaces the vanilla HUD elements with their DayZ equivalents.
     * <p>
     * Every replaced layer is cancelled unconditionally, not only when the mod drew
     * something. NeoForge gates these layers on its own {@code hideGui} and
     * survival-mode conditions, so a cancelled layer that nothing redraws is exactly
     * what F1 and creative mode should look like - whereas cancelling only on a
     * successful draw would leave vanilla's health and food rows visible with the
     * HUD hidden. The mod's own {@code renderHotbar} / {@code renderStatus} handle
     * {@code hideGui} and spectator mode internally and return false there, leaving
     * vanilla's rendered layers untouched.
     */
    @SubscribeEvent
    public static void onLayerPre(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        Identifier id = event.getName();

        if (VanillaGuiLayers.HOTBAR.equals(id)) {
            if (DayZHotbarHud.INSTANCE.renderHotbar(graphics, minecraft)) {
                event.setCanceled(true);
            }
        } else if (VanillaGuiLayers.PLAYER_HEALTH.equals(id)) {
            // The readout is drawn here rather than on one of the other status
            // layers because PLAYER_HEALTH is the one that leads the row. It
            // replaces the whole row, so where in the row it is anchored does not
            // matter - only that something draws it exactly once.
            if (DayZHotbarHud.INSTANCE.renderStatus(graphics, minecraft)) {
                event.setCanceled(true);
            }
        } else if (VanillaGuiLayers.EXPERIENCE_LEVEL.equals(id)
                || EXPERIENCE_LAYERS.contains(id)
                || REPLACED_BY_STATUS_ROW.contains(id)) {
            event.setCanceled(true);
        }
    }
}
