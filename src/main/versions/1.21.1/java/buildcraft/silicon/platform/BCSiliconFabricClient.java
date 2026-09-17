/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.platform;

import buildcraft.fabric.client.event.ClientPlayerNetworkEvent;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import buildcraft.silicon.BCSiliconBlocks;
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
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.NoopRenderer;

// laser overlay fires on Fabric's immediate WorldRenderEvents.AFTER_TRANSLUCENT (1.21.1 has no submit pipeline);
// package renderer registers via Fabric's registry since vanilla EntityRenderers.register is private here.
public final class BCSiliconFabricClient {
   private BCSiliconFabricClient() {
   }

   public static void init() {
      FabricModelModifyHooks.register(BCSiliconClient::onModifyBakingResult);
      WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> RenderLaser.onRenderLevel(
         new RenderLevelStageEvent.AfterTranslucentBlocks(context.matrixStack(), context.camera().getPosition())
      ));
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
      EntityRendererRegistry.register(BCSiliconEntities.PACKAGE, NoopRenderer::new);
      // without an explicit cutout layer the glass top draws on the solid layer and turns opaque.
      BlockRenderLayerMap.INSTANCE.putBlock(BCSiliconBlocks.PROGRAMMING_TABLE, RenderType.cutout());
   }
}
