package buildcraft.fabric;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;

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
import buildcraft.builders.gui.GuiArchitectTable;
import buildcraft.builders.gui.GuiBuilder;
import buildcraft.builders.gui.GuiElectronicLibrary;
import buildcraft.builders.gui.GuiFiller;
import buildcraft.builders.gui.GuiFillerPlanner;
import buildcraft.builders.gui.GuiReplacer;
import buildcraft.fabric.client.event.RenderLevelStageEvent;
import buildcraft.fabric.client.event.SubmitCustomGeometryEvent;

public final class BCBuildersFabricClient {
    private BCBuildersFabricClient() {}

    public static void init() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context -> {
            var stage = new RenderLevelStageEvent.AfterTranslucentBlocks(
                    context.poseStack(), context.levelState());
            BCBuildersEventDist.INSTANCE.renderAllQuarries(stage);
            BCBuildersEventDist.INSTANCE.renderAllFillers(stage);
            BCBuildersEventDist.INSTANCE.renderAllArchitectTables(stage);
            BCBuildersEventDist.INSTANCE.renderAllBuilders(stage);

            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer != null) {
                SubmitNodeStorage storage = mc.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage();
                var geometry = new SubmitCustomGeometryEvent(
                        context.poseStack(), context.levelState(), storage);
                BCBuildersEventDist.INSTANCE.renderAllFillersCustomGeometry(geometry);
                BCBuildersEventDist.INSTANCE.renderAllBuildersCustomGeometry(geometry);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client ->
                buildcraft.builders.snapshot.ClientArchitectScans.INSTANCE.tick());

        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.FILLER, GuiFiller::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.BUILDER, GuiBuilder::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.ARCHITECT, GuiArchitectTable::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.LIBRARY, GuiElectronicLibrary::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.REPLACER, GuiReplacer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCBuildersMenuTypes.FILLER_PLANNER, GuiFillerPlanner::new);

        net.minecraft.client.renderer.entity.EntityRenderers.register(
                BCBuildersEntities.QUARRY_RIG,
                net.minecraft.client.renderer.entity.NoopRenderer::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCBuildersBlockEntities.QUARRY, RenderQuarry::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCBuildersBlockEntities.ARCHITECT, RenderArchitectTable::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCBuildersBlockEntities.FILLER, RenderFiller::new);

        buildcraft.fabric.client.event.RenderTooltipEvent.Pre.register(BlueprintTooltipOverlay::onPreTooltip);
        buildcraft.fabric.client.event.RenderTooltipEvent.Pre.register(SchematicSingleTooltipOverlay::onPreTooltip);

        buildcraft.lib.client.BCTooltips.addTooltip(BCBuildersItems.QUARRY.get(), "tip.block.quarry");
    }
}

