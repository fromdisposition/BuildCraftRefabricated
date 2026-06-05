package buildcraft.fabric;

import buildcraft.api.items.IList;
import buildcraft.core.BCCoreBlockEntities;
import buildcraft.core.BCCoreItems;
import buildcraft.core.BCCoreMenuTypes;
import buildcraft.core.BCCoreModels;
import buildcraft.core.item.ItemMarkerConnector;
import buildcraft.core.list.GuiList;
import buildcraft.lib.client.render.MarkerRenderer;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.resources.Identifier;

public final class BCCoreFabricClient {
    private BCCoreFabricClient() {}

    public static void init() {
        MarkerRenderer.setVolumeBoxRenderCallback(
                buildcraft.core.client.VolumeBoxRenderer::renderAll);
        MarkerRenderer.setHoldingConnectorCheck(player ->
                player.getMainHandItem().getItem() instanceof ItemMarkerConnector
                        || player.getOffhandItem().getItem() instanceof ItemMarkerConnector);

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context ->
                MarkerRenderer.renderMarkers(
                        context.poseStack(),
                        context.levelState().cameraRenderState.pos));

        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCCoreBlockEntities.ENGINE_REDSTONE,
                ctx -> new RenderEngine_BC8(BCCoreModels::getWoodEngineQuads));
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCCoreBlockEntities.ENGINE_CREATIVE,
                ctx -> new RenderEngine_BC8(BCCoreModels::getCreativeEngineQuads));

        net.minecraft.client.gui.screens.MenuScreens.register(BCCoreMenuTypes.LIST, GuiList::new);

        buildcraft.core.list.ListTooltipHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client ->
                buildcraft.core.client.DebugOverlayHelper.onClientTick());
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("buildcraftcore", "debug_overlay"),
                buildcraft.core.client.DebugOverlayRenderer::render);

        buildcraft.lib.client.BCTooltips.init();
        buildcraft.lib.client.BCTooltips.addTooltip(BCCoreItems.ENGINE_CREATIVE, "tip.block.engine_creative");
        buildcraft.lib.client.BCTooltips.addTooltip(BCCoreItems.ENGINE_REDSTONE, "tip.block.engine_redstone");
        buildcraft.lib.client.BCTooltips.addTooltip(BCCoreItems.MARKER_VOLUME, "tip.block.marker_volume");
        buildcraft.lib.client.BCTooltips.addTooltip(BCCoreItems.MARKER_PATH, "tip.block.marker_path");
        if (buildcraft.lib.BCLib.DEV) {
            if (BCCoreItems.POWER_TESTER != null) {
                buildcraft.lib.client.BCTooltips.markDevOnly(BCCoreItems.POWER_TESTER);
            }
            if (BCCoreItems.GOGGLES != null) {
                buildcraft.lib.client.BCTooltips.markDevOnly(BCCoreItems.GOGGLES);
            }
            if (BCCoreItems.MAP_LOCATION != null) {
                buildcraft.lib.client.BCTooltips.markDevOnly(BCCoreItems.MAP_LOCATION);
            }
        }
    }
}
