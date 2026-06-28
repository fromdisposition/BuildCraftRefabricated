package buildcraft.silicon.platform;

import buildcraft.fabric.client.event.ClientPlayerNetworkEvent;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import buildcraft.silicon.BCSiliconEntities;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.client.BCSiliconClient;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiChargingTable;
import buildcraft.silicon.gui.GuiGate;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.gui.GuiPackager;
import buildcraft.silicon.gui.GuiProgrammingTable;
import buildcraft.silicon.gui.GuiStampingTable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
*///?}
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
//? if < 26.1 {
/*import buildcraft.silicon.BCSiliconBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
*///?}

public final class BCSiliconFabricClient {
   private BCSiliconFabricClient() {
   }

   public static void init() {
      FabricModelModifyHooks.register(BCSiliconClient::onModifyBakingResult);
      //? if >= 26.1 {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES
         .register(
            (AfterTranslucentFeatures)context -> RenderLaser.onRenderLevel(
               new RenderLevelStageEvent.AfterTranslucentBlocks(context.poseStack(), context.levelState())
            )
         );
      //?} else {
      /*WorldRenderEvents.END_MAIN.register(context -> RenderLaser.onRenderLevel(
         new RenderLevelStageEvent.AfterTranslucentBlocks(context.matrices(), context.worldState())
      ));
      *///?}
      ClientPlayConnectionEvents.JOIN
         .register((Join)(handler, sender, server) -> BCSiliconClient.GameBus.onClientLoggingIn(new ClientPlayerNetworkEvent.LoggingIn()));
      MenuScreens.register(BCSiliconMenuTypes.ASSEMBLY_TABLE, GuiAssemblyTable::new);
      MenuScreens.register(BCSiliconMenuTypes.INTEGRATION_TABLE, GuiIntegrationTable::new);
      MenuScreens.register(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, GuiAdvancedCraftingTable::new);
      MenuScreens.register(BCSiliconMenuTypes.CHARGING_TABLE, GuiChargingTable::new);
      MenuScreens.register(BCSiliconMenuTypes.PROGRAMMING_TABLE, GuiProgrammingTable::new);
      MenuScreens.register(BCSiliconMenuTypes.STAMPING_TABLE, GuiStampingTable::new);
      MenuScreens.register(BCSiliconMenuTypes.PACKAGER, GuiPackager::new);
      MenuScreens.register(BCSiliconMenuTypes.GATE, GuiGate::new);
      EntityRenderers.register(BCSiliconEntities.PACKAGE, NoopRenderer::new);
      //? if < 26.1 {
      /*// The programming table model has a glass top (minecraft:block/glass); without an explicit cutout
      // chunk layer it renders on the solid layer and the glass turns opaque (flat dark centre). 26.1+
      // resolves the render layer from the model/block, so this is only needed on <26.1.
      BlockRenderLayerMap.putBlock(BCSiliconBlocks.PROGRAMMING_TABLE, ChunkSectionLayer.CUTOUT);
      *///?}
   }
}
