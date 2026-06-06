package buildcraft.fabric;

import buildcraft.fabric.client.event.ClientPlayerNetworkEvent;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.client.BCSiliconClient;
import buildcraft.silicon.client.render.RenderLaser;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiGate;
import buildcraft.silicon.gui.GuiIntegrationTable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import net.minecraft.client.gui.screens.MenuScreens;

public final class BCSiliconFabricClient {
   private BCSiliconFabricClient() {
   }

   public static void init() {
      FabricModelModifyHooks.register(BCSiliconClient::onModifyBakingResult);
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES
         .register(
            (AfterTranslucentFeatures)context -> RenderLaser.onRenderLevel(
               new RenderLevelStageEvent.AfterTranslucentBlocks(context.poseStack(), context.levelState())
            )
         );
      ClientPlayConnectionEvents.JOIN
         .register((Join)(handler, sender, server) -> BCSiliconClient.GameBus.onClientLoggingIn(new ClientPlayerNetworkEvent.LoggingIn()));
      MenuScreens.register(BCSiliconMenuTypes.ASSEMBLY_TABLE, GuiAssemblyTable::new);
      MenuScreens.register(BCSiliconMenuTypes.INTEGRATION_TABLE, GuiIntegrationTable::new);
      MenuScreens.register(BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE, GuiAdvancedCraftingTable::new);
      MenuScreens.register(BCSiliconMenuTypes.GATE, GuiGate::new);
   }
}
