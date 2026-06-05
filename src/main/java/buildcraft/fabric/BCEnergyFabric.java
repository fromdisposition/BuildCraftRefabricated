package buildcraft.fabric;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.block.BlockSpring;
import buildcraft.energy.BCEnergyBlockEntities;
import buildcraft.energy.BCEnergyBlocks;
import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.BCEnergyMenuTypes;
import buildcraft.energy.BCEnergyRecipes;
import buildcraft.energy.tile.TileSpringOil;
import buildcraft.lib.attachments.Attachments;
import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.transfer.fluid.BucketResourceHandler;

import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class BCEnergyFabric {
    private BCEnergyFabric() {}

    public static void register() {
        BCEnergyFluidsFabric.register();
        buildcraft.energy.BCEnergyFluids.refreshSnapshot();
        BCEnergyBlocks.register();
        BCEnergyItems.register();
        BCEnergyBlockEntities.register();
        BCEnergyMenuTypes.register();

        registerCapabilities();
        registerBucketAttachments();

        EnumSpring.OIL.liquidBlock = BCEnergyFluidsFabric.sourceBlockState(BCEnergyFluidsFabric.OIL_COOL);
        EnumSpring.OIL.canGen = true;
        BlockSpring.oilTileFactory = TileSpringOil::new;

        ServerLifecycleEvents.SERVER_STARTING.register(server -> BCEnergyRecipes.init());

        BCEnergyWorldGenFabric.init();
    }

    public static void onConfigReloaded() {

    }

    private static void registerBucketAttachments() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();
        java.util.Set<Item> seen = new java.util.HashSet<>();
        for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
            Item bucket = entry.bucket();
            if (bucket != null && seen.add(bucket)) {
                event.registerItem(
                        Attachments.Fluid.ITEM,
                        (stack, access) -> new BucketResourceHandler(access),
                        bucket);
            }
        }
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        event.registerBlockEntity(
                Attachments.Fluid.BLOCK,
                BCEnergyBlockEntities.ENGINE_IRON,
                (engine, direction) -> direction == engine.getOrientation() ? null : engine.combinedFluidHandler);

        event.registerBlockEntity(
                Attachments.Item.BLOCK,
                BCEnergyBlockEntities.ENGINE_STONE,
                (engine, direction) -> direction == engine.getOrientation() ? null : engine.fuelItemHandler);

        event.registerBlockEntity(
                buildcraft.api.mj.MjAPI.CAP_CONNECTOR,
                BCEnergyBlockEntities.ENGINE_STONE,
                (engine, direction) -> engine.getMjConnector());

        event.registerBlockEntity(
                buildcraft.api.mj.MjAPI.CAP_CONNECTOR,
                BCEnergyBlockEntities.ENGINE_IRON,
                (engine, direction) -> engine.getMjConnector());

        event.registerBlockEntity(
                buildcraft.api.mj.MjAPI.CAP_CONNECTOR,
                BCEnergyBlockEntities.ENGINE_FE,
                (engine, direction) -> direction != engine.getOrientation() ? null : engine.getMjConnector());

        event.registerBlockEntity(
                buildcraft.api.mj.MjAPI.CAP_CONNECTOR,
                BCEnergyBlockEntities.DYNAMO_MJ,
                (dynamo, direction) -> direction == dynamo.getOrientation() ? null : dynamo.getMjConnector());

        event.registerBlockEntity(
                buildcraft.api.mj.MjAPI.CAP_RECEIVER,
                BCEnergyBlockEntities.DYNAMO_MJ,
                (dynamo, direction) -> {
                    if (direction == dynamo.getOrientation()) {
                        return null;
                    }
                    buildcraft.api.mj.IMjConnector connector = dynamo.getMjConnector();
                    return connector instanceof buildcraft.api.mj.IMjReceiver receiver ? receiver : null;
                });

        event.registerBlockEntity(
                Attachments.Energy.BLOCK,
                BCEnergyBlockEntities.ENGINE_FE,
                (engine, direction) -> direction == engine.getOrientation() ? null : engine.energyStorage);

        event.registerBlockEntity(
                Attachments.Energy.BLOCK,
                BCEnergyBlockEntities.DYNAMO_MJ,
                (dynamo, direction) -> direction != dynamo.getOrientation() ? null : dynamo.energyStorage);
    }
}
