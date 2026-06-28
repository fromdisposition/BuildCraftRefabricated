/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.platform;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.client.render.BCBuildersWorldRenderer;
import buildcraft.builders.client.render.RenderArchitectTable;
import buildcraft.builders.client.render.RenderFiller;
import buildcraft.builders.client.render.RenderQuarry;
import buildcraft.builders.client.tooltip.BlueprintTooltipOverlay;
import buildcraft.builders.client.tooltip.SchematicSingleTooltipOverlay;
import buildcraft.builders.gui.GuiArchitectTable;
import buildcraft.builders.gui.GuiBuilder;
import buildcraft.builders.gui.GuiElectronicLibrary;
import buildcraft.builders.gui.GuiFiller;
import buildcraft.builders.gui.GuiFillerPlanner;
import buildcraft.builders.gui.GuiReplacer;
import buildcraft.builders.snapshot.ClientArchitectScans;
import buildcraft.builders.tooltip.BlueprintPreviewTooltipComponent;
import buildcraft.builders.tooltip.SchematicPreviewTooltipComponent;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.fabric.client.event.SubmitCustomGeometryEvent;
import buildcraft.lib.client.BCTooltips;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.world.phys.Vec3;

/**
 * 1.21.1 implementation (versions/1.21.1). 1.21.1 has no deferred submit pipeline or level render-state, so the
 * builder/quarry/filler in-world overlays fire on Fabric's immediate WorldRenderEvents.AFTER_TRANSLUCENT, passing
 * the live MultiBufferSource and camera position to the version-neutral events. Block render layers use the
 * 1.21.1 BlockRenderLayerMap (RenderType), and the noop entity renderer registers via Fabric's registry.
 */
public final class BCBuildersFabricClient {
   private BCBuildersFabricClient() {
   }

   public static void init() {
      WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
         Vec3 cameraPos = context.camera().getPosition();
         RenderLevelStageEvent.AfterTranslucentBlocks stage = new RenderLevelStageEvent.AfterTranslucentBlocks(context.matrixStack(), cameraPos);
         BCBuildersWorldRenderer.renderAllQuarries(stage);
         BCBuildersWorldRenderer.renderAllFillers(stage);
         BCBuildersWorldRenderer.renderAllArchitectTables(stage);
         BCBuildersWorldRenderer.renderAllBuilders(stage);
         SubmitCustomGeometryEvent geometry = new SubmitCustomGeometryEvent(context.matrixStack(), cameraPos, context.consumers());
         BCBuildersWorldRenderer.renderAllFillersCustomGeometry(geometry);
         BCBuildersWorldRenderer.renderAllBuildersCustomGeometry(geometry);
      });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> ClientArchitectScans.INSTANCE.tick());
      MenuScreens.register(BCBuildersMenuTypes.FILLER, GuiFiller::new);
      MenuScreens.register(BCBuildersMenuTypes.BUILDER, GuiBuilder::new);
      MenuScreens.register(BCBuildersMenuTypes.ARCHITECT, GuiArchitectTable::new);
      MenuScreens.register(BCBuildersMenuTypes.LIBRARY, GuiElectronicLibrary::new);
      MenuScreens.register(BCBuildersMenuTypes.REPLACER, GuiReplacer::new);
      MenuScreens.register(BCBuildersMenuTypes.FILLER_PLANNER, GuiFillerPlanner::new);
      EntityRendererRegistry.register(BCBuildersEntities.QUARRY_RIG, NoopRenderer::new);
      BlockEntityRenderers.register(BCBuildersBlockEntities.QUARRY, RenderQuarry::new);
      BlockEntityRenderers.register(BCBuildersBlockEntities.ARCHITECT, RenderArchitectTable::new);
      BlockEntityRenderers.register(BCBuildersBlockEntities.FILLER, RenderFiller::new);
      ClientTooltipComponentCallback.EVENT.register(component -> {
         if (component instanceof BlueprintPreviewTooltipComponent preview) {
            return new BlueprintTooltipOverlay(preview);
         } else if (component instanceof SchematicPreviewTooltipComponent preview) {
            return new SchematicSingleTooltipOverlay(preview);
         }

         return null;
      });
      BCTooltips.addTooltip(BCBuildersItems.QUARRY, "tip.block.quarry");
      // 1.21.1 ignores the model JSON "render_type"; register the cutout layer so the frame lattice and
      // builder slots render with transparency instead of falling back to SOLID.
      BlockRenderLayerMap.INSTANCE.putBlock(BCBuildersBlocks.FRAME, RenderType.cutout());
      BlockRenderLayerMap.INSTANCE.putBlock(BCBuildersBlocks.BUILDER, RenderType.cutout());
   }
}
