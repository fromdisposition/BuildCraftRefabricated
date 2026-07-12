package buildcraft.core.platform;

import buildcraft.core.BCCoreBlockEntities;
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
import buildcraft.lib.client.render.laser.LaserBatch;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
*///?}
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if < 26.1 {
/*import buildcraft.core.BCCoreBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
*///?}
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeStorage;
//?}
import net.minecraft.resources.Identifier;

public final class BCCoreFabricClient {
   private BCCoreFabricClient() {
   }

   public static void init() {
      MarkerRenderer.setVolumeBoxRenderCallback(VolumeBoxRenderer::renderAll);
      MarkerRenderer.setHoldingConnectorCheck(
         player -> player.getMainHandItem().getItem() instanceof ItemMarkerConnector || player.getOffhandItem().getItem() instanceof ItemMarkerConnector
      );
      //? if >= 26.2 {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         LaserBatch.setNodeStorage((SubmitNodeStorage) context.submitNodeCollector());
         MarkerRenderer.renderMarkers(context.poseStack(), context.levelState().cameraRenderState.pos);
      });
      //?} else if >= 26.1 {
      /*LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context ->
         MarkerRenderer.renderMarkers(context.poseStack(), context.levelState().cameraRenderState.pos)
      );
      *///?} else {
      /*// 1.21.x has no AfterTranslucent phase; END_MAIN runs after the main (incl. translucent) pass.
      WorldRenderEvents.END_MAIN.register(
         context -> MarkerRenderer.renderMarkers(context.matrices(), context.gameRenderer().getMainCamera().position())
      );
      *///?}
      BlockEntityRenderers.register(BCCoreBlockEntities.ENGINE_REDSTONE, ctx -> new RenderEngine_BC8(BCCoreModels::getWoodEngineQuads));
      BlockEntityRenderers.register(BCCoreBlockEntities.ENGINE_CREATIVE, ctx -> new RenderEngine_BC8(BCCoreModels::getCreativeEngineQuads));
      MenuScreens.register(BCCoreMenuTypes.LIST, GuiList::new);
      ListTooltipHandler.register();
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> DebugOverlayHelper.onClientTick());
      HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("buildcraftcore", "debug_overlay"), DebugOverlayRenderer::render);
      BCTooltips.init();
      BCTooltips.addTooltip(BCCoreItems.ENGINE_CREATIVE, "tip.block.engine_creative");
      BCTooltips.addTooltip(BCCoreItems.ENGINE_REDSTONE, "tip.block.engine_redstone");
      BCTooltips.addTooltip(BCCoreItems.MARKER_VOLUME, "tip.block.marker_volume");
      BCTooltips.addTooltip(BCCoreItems.MARKER_PATH, "tip.block.marker_path");
      if (BCCoreItems.POWER_TESTER != null) {
         BCTooltips.addTooltip(BCCoreItems.POWER_TESTER, "tip.block.power_tester");
      }

      //? if < 26.1 {
      /*// Path/volume markers use the torch-cross model (transparent corners); they need the cutout chunk
      // layer on <26.1 or the transparent pixels render opaque black. 26.1+ derives this from the model.
      BlockRenderLayerMap.putBlock(BCCoreBlocks.MARKER_PATH, ChunkSectionLayer.CUTOUT);
      BlockRenderLayerMap.putBlock(BCCoreBlocks.MARKER_VOLUME, ChunkSectionLayer.CUTOUT);
      *///?}
   }
}
