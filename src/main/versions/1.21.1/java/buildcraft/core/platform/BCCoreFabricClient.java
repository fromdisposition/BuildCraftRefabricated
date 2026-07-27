/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.platform;

import buildcraft.core.BCCoreBlockEntities;
import buildcraft.core.BCCoreBlocks;
import buildcraft.core.BCCoreItems;
import buildcraft.core.BCCoreMenuTypes;
import buildcraft.core.BCCoreModels;
import buildcraft.core.client.DebugOverlayHelper;
import buildcraft.core.client.DebugOverlayRenderer;
import buildcraft.core.client.VolumeBoxRenderer;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.list.GuiList;
import buildcraft.core.list.ListTooltipHandler;
import buildcraft.lib.client.BCTooltips;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

/**
 * 1.21.1 implementation (versions/1.21.1). Markers render on Fabric's immediate WorldRenderEvents.AFTER_TRANSLUCENT
 * (1.21.1 has no AfterTranslucentFeatures phase / submit pipeline), and the debug overlay registers via the legacy
 * HudRenderCallback (the 1.21.5+ HudElementRegistry does not exist on 1.21.1).
 */
public final class BCCoreFabricClient {
   private BCCoreFabricClient() {
   }

   public static void init() {
      MarkerRenderer.setVolumeBoxRenderCallback(VolumeBoxRenderer::renderAll);
      MarkerRenderer.setHoldingConnectorCheck(
         player -> player.getMainHandItem().getItem() instanceof ItemMarkerConnector || player.getOffhandItem().getItem() instanceof ItemMarkerConnector
      );
      WorldRenderEvents.AFTER_TRANSLUCENT.register(
         context -> MarkerRenderer.renderMarkers(context.matrixStack(), context.camera().getPosition())
      );
      // Marker blocks use a partly transparent texture (the coloured post sides); without an explicit cutout
      // render layer they draw on the solid layer and the transparent pixels turn black. 1.21.1 resolves render
      // layers via Fabric's BlockRenderLayerMap (RenderType, not the 26.1+ ChunkSectionLayer).
      BlockRenderLayerMap.INSTANCE.putBlock(BCCoreBlocks.MARKER_PATH, RenderType.cutout());
      BlockRenderLayerMap.INSTANCE.putBlock(BCCoreBlocks.MARKER_VOLUME, RenderType.cutout());
      BlockEntityRenderers.register(BCCoreBlockEntities.ENGINE_REDSTONE, ctx -> new RenderEngine_BC8(BCCoreModels::getWoodEngineQuads));
      BlockEntityRenderers.register(BCCoreBlockEntities.ENGINE_CREATIVE, ctx -> new RenderEngine_BC8(BCCoreModels::getCreativeEngineQuads));
      MenuScreens.register(BCCoreMenuTypes.LIST, GuiList::new);
      ListTooltipHandler.register();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> DebugOverlayHelper.onClientTick());
      HudRenderCallback.EVENT.register(DebugOverlayRenderer::render);
      BCTooltips.init();
      BCTooltips.addTooltip(BCCoreItems.ENGINE_CREATIVE, "tip.block.engine_creative");
      BCTooltips.addTooltip(BCCoreItems.ENGINE_REDSTONE, "tip.block.engine_redstone");
      BCTooltips.addTooltip(BCCoreItems.MARKER_VOLUME, "tip.block.marker_volume");
      BCTooltips.addTooltip(BCCoreItems.MARKER_PATH, "tip.block.marker_path");
      if (BCCoreItems.POWER_TESTER != null) {
         BCTooltips.addTooltip(BCCoreItems.POWER_TESTER, "tip.block.power_tester");
      }
   }
}
