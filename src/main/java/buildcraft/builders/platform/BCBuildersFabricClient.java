package buildcraft.builders.platform;

import buildcraft.builders.BCBuildersBlockEntities;
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
import buildcraft.builders.tooltip.BlueprintPreviewTooltipComponent;
import buildcraft.builders.tooltip.SchematicPreviewTooltipComponent;
import buildcraft.builders.gui.GuiArchitectTable;
import buildcraft.builders.gui.GuiBuilder;
import buildcraft.builders.gui.GuiElectronicLibrary;
import buildcraft.builders.gui.GuiFiller;
import buildcraft.builders.gui.GuiFillerPlanner;
import buildcraft.builders.gui.GuiReplacer;
import buildcraft.builders.snapshot.ClientArchitectScans;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.fabric.client.event.SubmitCustomGeometryEvent;
import buildcraft.lib.client.BCTooltips;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
//? if >= 26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
*///?}
import buildcraft.lib.client.render.laser.LaserBatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
//? if >= 26.2 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
//?} else {
/*import net.minecraft.client.renderer.SubmitNodeStorage;
*///?}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
//? if < 26.1 {
/*import buildcraft.builders.BCBuildersBlocks;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
*///?}

public final class BCBuildersFabricClient {
   private BCBuildersFabricClient() {
   }

   public static void init() {
      //? if >= 26.2 {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         LaserBatch.setNodeStorage((SubmitNodeStorage) context.submitNodeCollector());
         RenderLevelStageEvent.AfterTranslucentBlocks stage = new RenderLevelStageEvent.AfterTranslucentBlocks(context.poseStack(), context.levelState());
         BCBuildersWorldRenderer.renderAllQuarries(stage);
         BCBuildersWorldRenderer.renderAllFillers(stage);
         BCBuildersWorldRenderer.renderAllArchitectTables(stage);
         BCBuildersWorldRenderer.renderAllBuilders(stage);
         SubmitCustomGeometryEvent geometry = new SubmitCustomGeometryEvent(context.poseStack(), context.levelState(), context.submitNodeCollector());
         BCBuildersWorldRenderer.renderAllFillersCustomGeometry(geometry);
         BCBuildersWorldRenderer.renderAllBuildersCustomGeometry(geometry);
      });
      //?} else if >= 26.1 {
      /*LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         RenderLevelStageEvent.AfterTranslucentBlocks stage = new RenderLevelStageEvent.AfterTranslucentBlocks(context.poseStack(), context.levelState());
         BCBuildersWorldRenderer.renderAllQuarries(stage);
         BCBuildersWorldRenderer.renderAllFillers(stage);
         BCBuildersWorldRenderer.renderAllArchitectTables(stage);
         BCBuildersWorldRenderer.renderAllBuilders(stage);
         Minecraft mc = Minecraft.getInstance();
         if (mc.gameRenderer != null) {
            SubmitNodeStorage storage = mc.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage();
            SubmitCustomGeometryEvent geometry = new SubmitCustomGeometryEvent(context.poseStack(), context.levelState(), storage);
            BCBuildersWorldRenderer.renderAllFillersCustomGeometry(geometry);
            BCBuildersWorldRenderer.renderAllBuildersCustomGeometry(geometry);
         }
      });
      *///?} else {
      /*// 1.21.x: render in END_MAIN; WorldRenderContext.commandQueue() is the SubmitNodeCollector.
      WorldRenderEvents.END_MAIN.register(context -> {
         RenderLevelStageEvent.AfterTranslucentBlocks stage = new RenderLevelStageEvent.AfterTranslucentBlocks(context.matrices(), context.worldState());
         BCBuildersWorldRenderer.renderAllQuarries(stage);
         BCBuildersWorldRenderer.renderAllFillers(stage);
         BCBuildersWorldRenderer.renderAllArchitectTables(stage);
         BCBuildersWorldRenderer.renderAllBuilders(stage);
         SubmitCustomGeometryEvent geometry = new SubmitCustomGeometryEvent(context.matrices(), context.worldState(), context.commandQueue());
         BCBuildersWorldRenderer.renderAllFillersCustomGeometry(geometry);
         BCBuildersWorldRenderer.renderAllBuildersCustomGeometry(geometry);
      });
      *///?}
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> ClientArchitectScans.INSTANCE.tick());
      MenuScreens.register(BCBuildersMenuTypes.FILLER, GuiFiller::new);
      MenuScreens.register(BCBuildersMenuTypes.BUILDER, GuiBuilder::new);
      MenuScreens.register(BCBuildersMenuTypes.ARCHITECT, GuiArchitectTable::new);
      MenuScreens.register(BCBuildersMenuTypes.LIBRARY, GuiElectronicLibrary::new);
      MenuScreens.register(BCBuildersMenuTypes.REPLACER, GuiReplacer::new);
      MenuScreens.register(BCBuildersMenuTypes.FILLER_PLANNER, GuiFillerPlanner::new);
      EntityRenderers.register(BCBuildersEntities.QUARRY_RIG, NoopRenderer::new);
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
      // 1.21.x ignores the model JSON "render_type" field; register the cutout layer explicitly so
      // the frame lattice and builder slots render with transparency instead of falling back to SOLID.
      //? if < 26.1 {
      /*BlockRenderLayerMap.putBlock(BCBuildersBlocks.FRAME, ChunkSectionLayer.CUTOUT);
      BlockRenderLayerMap.putBlock(BCBuildersBlocks.BUILDER, ChunkSectionLayer.CUTOUT);
      *///?}
   }
}
