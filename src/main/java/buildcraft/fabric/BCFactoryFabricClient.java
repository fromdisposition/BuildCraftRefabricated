package buildcraft.fabric;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.client.render.RenderDistiller;
import buildcraft.factory.client.render.RenderHeatExchange;
import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.client.render.TubeRenderer;
import buildcraft.factory.gui.GuiAutoCraftFluids;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.factory.gui.GuiTank;
import buildcraft.lib.client.BCTooltips;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class BCFactoryFabricClient {
   private BCFactoryFabricClient() {
   }

   public static void init() {
      MenuScreens.register(BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, GuiAutoCraftItems::new);
      if (BCFactoryMenuTypes.AUTO_WORKBENCH_FLUIDS != null) {
         MenuScreens.register(BCFactoryMenuTypes.AUTO_WORKBENCH_FLUIDS, GuiAutoCraftFluids::new);
      }
      MenuScreens.register(BCFactoryMenuTypes.TANK, GuiTank::new);
      MenuScreens.register(BCFactoryMenuTypes.CHUTE, GuiChute::new);
      MenuScreens.register(BCFactoryMenuTypes.DISTILLER, GuiDistiller::new);
      MenuScreens.register(BCFactoryMenuTypes.HEAT_EXCHANGE, GuiHeatExchange::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.TANK, RenderTank::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.DISTILLER, RenderDistiller::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.HEAT_EXCHANGE, RenderHeatExchange::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.PUMP, RenderPump::new);
      BlockEntityRenderers.register(BCFactoryBlockEntities.MINING_WELL, RenderMiningWell::new);
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES
         .register(
            (AfterTranslucentFeatures)context -> TubeRenderer.onRenderLevel(
               context.poseStack(), context.levelState().cameraRenderState.pos, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)
            )
         );
      registerTooltips();
   }

   private static void registerTooltips() {
      BCTooltips.addTooltip(BCFactoryItems.AUTOWORKBENCH_ITEM, "tip.block.autoworkbench_item");
      if (BCFactoryItems.AUTOWORKBENCH_FLUID != null) {
         BCTooltips.addTooltip(BCFactoryItems.AUTOWORKBENCH_FLUID, "tip.block.autoworkbench_fluid");
         BCTooltips.markDevOnly(BCFactoryItems.AUTOWORKBENCH_FLUID);
      }
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
