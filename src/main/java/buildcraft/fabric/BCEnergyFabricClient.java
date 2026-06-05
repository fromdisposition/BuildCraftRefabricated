package buildcraft.fabric;

import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.BCEnergyModels;
import buildcraft.energy.client.gui.GuiDynamoMJ;
import buildcraft.energy.client.gui.GuiEngineRF;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.energy.tile.TileDynamoMJ;
import buildcraft.energy.tile.TileEngineRF;
import buildcraft.energy.tile.TileEngineIron_BC8;
import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.client.render.tile.RenderEngine_BC8;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.misc.FluidUtilBC;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public final class BCEnergyFabricClient {
    private BCEnergyFabricClient() {}

    public static void init() {
        for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
            Material stillMaterial = new Material(BcFluidTintUtil.bakedStillSpriteId(entry.name()));
            Material flowMaterial = new Material(BcFluidTintUtil.bakedFlowSpriteId(entry.name()));

            FluidModel.Unbaked model = new FluidModel.Unbaked(
                    stillMaterial,
                    flowMaterial,
                    null,
                    BlockTintSources.constant(BcFluidTintUtil.RENDER_TINT_WHITE));

            FluidRenderingRegistry.register(entry.still(), entry.flowing(), model);
            FluidRenderingRegistry.setBlockTransparency(entry.block(), FluidUtilBC.shouldRenderTranslucent(entry.still()));
        }

        net.minecraft.client.gui.screens.MenuScreens.register(
                BCEnergyMenuTypes.ENGINE_STONE, GuiEngineStone_BC8::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                BCEnergyMenuTypes.ENGINE_IRON, GuiEngineIron_BC8::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                BCEnergyMenuTypes.ENGINE_FE, GuiEngineRF::new);
        net.minecraft.client.gui.screens.MenuScreens.register(
                BCEnergyMenuTypes.DYNAMO_MJ, GuiDynamoMJ::new);

        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCEnergyBlockEntities.ENGINE_STONE,
                ctx -> new RenderEngine_BC8((engine, pt) -> {
                    if (engine instanceof TileEngineStone_BC8 stone) {
                        return BCEnergyModels.getStoneEngineQuads(stone, pt);
                    }
                    return MutableQuad.EMPTY_ARRAY;
                }));
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCEnergyBlockEntities.ENGINE_IRON,
                ctx -> new RenderEngine_BC8((engine, pt) -> {
                    if (engine instanceof TileEngineIron_BC8 iron) {
                        return BCEnergyModels.getIronEngineQuads(iron, pt);
                    }
                    return MutableQuad.EMPTY_ARRAY;
                }));
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCEnergyBlockEntities.ENGINE_FE,
                ctx -> new RenderEngine_BC8((engine, pt) -> {
                    if (engine instanceof TileEngineRF fe) {
                        return BCEnergyModels.getFeEngineQuads(fe, pt);
                    }
                    return MutableQuad.EMPTY_ARRAY;
                }));
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                BCEnergyBlockEntities.DYNAMO_MJ,
                ctx -> new RenderEngine_BC8((engine, pt) -> {
                    if (engine instanceof TileDynamoMJ dynamo) {
                        return BCEnergyModels.getDynamoQuads(dynamo, pt);
                    }
                    return MutableQuad.EMPTY_ARRAY;
                }));

        buildcraft.lib.client.BCTooltips.addTooltip(
                buildcraft.energy.BCEnergyItems.ENGINE_STONE, "tip.block.engine_stone");
        buildcraft.lib.client.BCTooltips.addTooltip(
                buildcraft.energy.BCEnergyItems.ENGINE_IRON, "tip.block.engine_iron");
        buildcraft.lib.client.BCTooltips.addTooltip(
                buildcraft.energy.BCEnergyItems.ENGINE_FE, "tip.block.engine_rf");
        buildcraft.lib.client.BCTooltips.addTooltip(
                buildcraft.energy.BCEnergyItems.DYNAMO_MJ, "tip.block.mj_dynamo");
    }
}
