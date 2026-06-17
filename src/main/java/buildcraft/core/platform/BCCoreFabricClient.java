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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
//? if >= 26.1.3 {
/*import net.minecraft.client.renderer.SubmitNodeStorage;*/
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
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES
         .register(
            (AfterTranslucentFeatures)context -> {
               //? if >= 26.1.3 {
               /*LaserBatch.setNodeStorage((SubmitNodeStorage) context.submitNodeCollector());*/
               //?}
               MarkerRenderer.renderMarkers(context.poseStack(), context.levelState().cameraRenderState.pos);
            }
         );
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

   }
}
