package buildcraft.factory.platform;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryEntities;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.client.render.RenderDistiller;
import buildcraft.factory.client.render.RenderHeatExchange;
import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.factory.gui.GuiTank;
import buildcraft.lib.client.BCTooltips;
//? if < 26.1 {
/*import buildcraft.factory.BCFactoryBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
*///?}
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class BCFactoryFabricClient {
   private BCFactoryFabricClient() {
   }

   public static void init() {
      MenuScreens.register(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, GuiAutoCraftItems::new);
      MenuScreens.register(BCFactoryMenuTypes.TANK, GuiTank::new);
      MenuScreens.register(BCFactoryMenuTypes.CHUTE, GuiChute::new);
      MenuScreens.register(BCFactoryMenuTypes.DISTILLER, GuiDistiller::new);
      MenuScreens.register(BCFactoryMenuTypes.HEAT_EXCHANGE, GuiHeatExchange::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.TANK, RenderTank::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.DISTILLER, RenderDistiller::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.HEAT_EXCHANGE, RenderHeatExchange::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.PUMP, RenderPump::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.MINING_WELL, RenderMiningWell::new);
      EntityRenderers.register(BCFactoryEntities.MINER_SHAFT, NoopRenderer::new);
      // 1.21.x ignores the model JSON "render_type" field (a 26.1 model-system feature), so the
      // chunk render layer must be registered explicitly via Fabric's BlockRenderLayerMap, otherwise
      // these blocks fall back to SOLID and their glass/lattice parts render opaque.
      //? if < 26.1 {
      /*BlockRenderLayerMap.putBlock(BCFactoryBlocks.TANK, ChunkSectionLayer.CUTOUT);
      BlockRenderLayerMap.putBlock(BCFactoryBlocks.DISTILLER, ChunkSectionLayer.CUTOUT);
      BlockRenderLayerMap.putBlock(BCFactoryBlocks.HEAT_EXCHANGE, ChunkSectionLayer.TRANSLUCENT);
      *///?}
      registerTooltips();
   }

   private static void registerTooltips() {
      BCTooltips.addTooltip(BCFactoryItems.AUTOWORKBENCH_ITEM, "tip.block.autoworkbench_item");
      BCTooltips.addTooltip(BCFactoryItems.MINING_WELL, "tip.block.mining_well");
      BCTooltips.addTooltip(BCFactoryItems.PUMP, "tip.block.pump");
      BCTooltips.addTooltip(BCFactoryItems.FLOOD_GATE, "tip.block.flood_gate");
      BCTooltips.addTooltip(BCFactoryItems.TANK, "tip.block.tank");
      BCTooltips.addTooltip(BCFactoryItems.CHUTE, "tip.block.chute");
      BCTooltips.addTooltip(BCFactoryItems.DISTILLER, "tip.block.distiller");
      BCTooltips.addTooltip(BCFactoryItems.HEAT_EXCHANGE, "tip.block.heat_exchange");
      BCTooltips.addTooltip(BCFactoryItems.WATER_GEL_SPAWN, "tip.item.water_gel_spawn");
      BCTooltips.addTooltip(BCFactoryItems.GELLED_WATER, "tip.item.gelled_water");
   }
}
