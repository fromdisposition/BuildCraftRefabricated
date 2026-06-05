package buildcraft.fabric;

import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiChute;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.factory.gui.GuiTank;
import buildcraft.factory.client.render.RenderDistiller;
import buildcraft.factory.client.render.RenderHeatExchange;
import buildcraft.factory.client.render.RenderMiningWell;
import buildcraft.factory.client.render.RenderPump;
import buildcraft.factory.client.render.RenderTank;
import buildcraft.factory.client.render.TubeRenderer;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public final class BCFactoryFabricClient {
    private BCFactoryFabricClient() {}

    public static void init() {
        net.minecraft.client.gui.screens.MenuScreens.register(
                BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS, GuiAutoCraftItems::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCFactoryMenuTypes.TANK, GuiTank::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCFactoryMenuTypes.CHUTE, GuiChute::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCFactoryMenuTypes.DISTILLER, GuiDistiller::new);
        net.minecraft.client.gui.screens.MenuScreens.register(BCFactoryMenuTypes.HEAT_EXCHANGE, GuiHeatExchange::new);

        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCFactoryBlockEntities.TANK, RenderTank::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCFactoryBlockEntities.DISTILLER, RenderDistiller::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCFactoryBlockEntities.HEAT_EXCHANGE, RenderHeatExchange::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCFactoryBlockEntities.PUMP, RenderPump::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCFactoryBlockEntities.MINING_WELL, RenderMiningWell::new);

        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(context ->
                TubeRenderer.onRenderLevel(
                        context.poseStack(),
                        context.levelState().cameraRenderState.pos,
                        net.minecraft.client.Minecraft.getInstance()
                                .getDeltaTracker()
                                .getGameTimeDeltaPartialTick(false)));

        registerTooltips();
    }

    private static void registerTooltips() {
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.AUTOWORKBENCH_ITEM, "tip.block.autoworkbench_item");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.MINING_WELL, "tip.block.mining_well");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.PUMP, "tip.block.pump");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.FLOOD_GATE, "tip.block.flood_gate");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.TANK, "tip.block.tank");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.CHUTE, "tip.block.chute");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.DISTILLER, "tip.block.distiller");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.HEAT_EXCHANGE, "tip.block.heat_exchange");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.WATER_GEL_SPAWN, "tip.item.water_gel_spawn");
        buildcraft.lib.client.BCTooltips.addTooltip(BCFactoryItems.GELLED_WATER, "tip.item.gelled_water");
    }
}
