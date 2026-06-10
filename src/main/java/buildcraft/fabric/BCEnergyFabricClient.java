package buildcraft.fabric;

import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.client.gui.GuiDynamoMJ;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineRF;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.lib.client.BCTooltips;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import buildcraft.lib.engine.TileEngineBase_BC8;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.block.FluidModel.Unbaked;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BCEnergyFabricClient {
   private BCEnergyFabricClient() {
   }

   public static void init() {
      for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
         Material stillMaterial = new Material(BcFluidTintUtil.bakedStillSpriteId(entry.name()));
         Material flowMaterial = new Material(BcFluidTintUtil.bakedFlowSpriteId(entry.name()));
         Unbaked model = new Unbaked(stillMaterial, flowMaterial, null, BlockTintSources.constant(-1));
         FluidRenderingRegistry.register(entry.still(), entry.flowing(), model);
         FluidRenderingRegistry.setBlockTransparency(entry.block(), true);
      }

      MenuScreens.register(BCEnergyMenuTypes.ENGINE_STONE, GuiEngineStone_BC8::new);
      MenuScreens.register(BCEnergyMenuTypes.ENGINE_IRON, GuiEngineIron_BC8::new);
      if (BCEnergyMenuTypes.ENGINE_FE != null) {
         MenuScreens.register(BCEnergyMenuTypes.ENGINE_FE, GuiEngineRF::new);
      }

      if (BCEnergyMenuTypes.DYNAMO_MJ != null) {
         MenuScreens.register(BCEnergyMenuTypes.DYNAMO_MJ, GuiDynamoMJ::new);
      }

      registerEngineBer(BCEnergyBlockEntities.ENGINE_STONE, BCEnergyModels::getStoneEngineQuads);
      registerEngineBer(BCEnergyBlockEntities.ENGINE_IRON, BCEnergyModels::getIronEngineQuads);
      if (BCEnergyBlockEntities.ENGINE_FE != null) {
         registerEngineBer(BCEnergyBlockEntities.ENGINE_FE, BCEnergyModels::getFeEngineQuads);
      }

      if (BCEnergyBlockEntities.DYNAMO_MJ != null) {
         registerEngineBer(BCEnergyBlockEntities.DYNAMO_MJ, BCEnergyModels::getDynamoQuads);
      }

      BCTooltips.addTooltip(BCEnergyItems.ENGINE_STONE, "tip.block.engine_stone");
      BCTooltips.addTooltip(BCEnergyItems.ENGINE_IRON, "tip.block.engine_iron");
      if (BCEnergyItems.ENGINE_FE != null) {
         BCTooltips.addTooltip(BCEnergyItems.ENGINE_FE, "tip.block.engine_rf");
      }

      if (BCEnergyItems.DYNAMO_MJ != null) {
         BCTooltips.addTooltip(BCEnergyItems.DYNAMO_MJ, "tip.block.mj_dynamo");
      }
   }

   @SuppressWarnings("unchecked")
   private static <T extends TileEngineBase_BC8> void registerEngineBer(BlockEntityType<T> type, BiFunction<T, Float, MutableQuad[]> quads) {
      BlockEntityRenderers.register(type, ctx -> new RenderEngine_BC8((engine, pt) -> quads.apply((T)engine, pt)));
   }
}
