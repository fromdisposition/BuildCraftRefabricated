package buildcraft.builders.platform;

import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class BCBuildersFabricClient {
   private BCBuildersFabricClient() {
   }

   public static void init() {
      LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register((AfterTranslucentFeatures)context -> {
         RenderLevelStageEvent.AfterTranslucentBlocks stage = new RenderLevelStageEvent.AfterTranslucentBlocks(context.poseStack(), context.levelState());
         BCBuildersEventDist.INSTANCE.renderAllQuarries(stage);
         BCBuildersEventDist.INSTANCE.renderAllFillers(stage);
         BCBuildersEventDist.INSTANCE.renderAllArchitectTables(stage);
         BCBuildersEventDist.INSTANCE.renderAllBuilders(stage);
         Minecraft mc = Minecraft.getInstance();
         if (mc.gameRenderer != null) {
            SubmitNodeStorage storage = mc.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage();
            SubmitCustomGeometryEvent geometry = new SubmitCustomGeometryEvent(context.poseStack(), context.levelState(), storage);
            BCBuildersEventDist.INSTANCE.renderAllFillersCustomGeometry(geometry);
            BCBuildersEventDist.INSTANCE.renderAllBuildersCustomGeometry(geometry);
         }
      });
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
   }
}
