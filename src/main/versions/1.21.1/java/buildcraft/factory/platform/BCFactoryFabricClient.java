/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.platform;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
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
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;

/**
 * 1.21.1 implementation (versions/1.21.1). Registers screens + (stub) block-entity renderers, the noop miner
 * shaft entity renderer via Fabric's registry (vanilla EntityRenderers.register is private on 1.21.1), and the
 * cutout/translucent block render layers via the 1.21.1 BlockRenderLayerMap (RenderType, not ChunkSectionLayer).
 */
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
      EntityRendererRegistry.register(BCFactoryEntities.MINER_SHAFT, NoopRenderer::new);
      BlockRenderLayerMap.INSTANCE.putBlock(BCFactoryBlocks.TANK, RenderType.cutout());
      BlockRenderLayerMap.INSTANCE.putBlock(BCFactoryBlocks.DISTILLER, RenderType.cutout());
      BlockRenderLayerMap.INSTANCE.putBlock(BCFactoryBlocks.HEAT_EXCHANGE, RenderType.translucent());
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
