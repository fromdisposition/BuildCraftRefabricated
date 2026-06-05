package buildcraft.fabric;

import buildcraft.api.mj.MjAPI;
import buildcraft.factory.BCFactoryAttachments;
import buildcraft.factory.BCFactoryBlockEntities;
import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.tile.TankColumnResourceHandler;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.attachments.Attachments;

import buildcraft.lib.attachments.RegisterAttachmentsEvent;
import buildcraft.lib.mj.MjBatteryEnergyHandler;
import buildcraft.lib.transfer.EmptyResourceHandler;
import buildcraft.lib.transfer.ResourceHandler;
import buildcraft.lib.transfer.fluid.FluidResource;

public final class BCFactoryFabric {
    private BCFactoryFabric() {}

    public static void register() {
        BCFactoryBlocks.register();
        BCFactoryItems.register();
        BCFactoryBlockEntities.register();
        BCFactoryMenuTypes.register();
        BCFactoryAttachments.register();
        registerCapabilities();
    }

    private static void registerCapabilities() {
        RegisterAttachmentsEvent event = new RegisterAttachmentsEvent();

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS,
                (workbench, direction) -> workbench.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS,
                (workbench, direction) -> workbench.getMjReceiver());
        event.registerBlockEntity(Attachments.Item.BLOCK, BCFactoryBlockEntities.AUTO_WORKBENCH_ITEMS,
                (workbench, direction) -> workbench.getItemHandler(direction));

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCFactoryBlockEntities.MINING_WELL,
                (miner, direction) -> miner.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCFactoryBlockEntities.MINING_WELL,
                (miner, direction) -> miner.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCFactoryBlockEntities.MINING_WELL,
                (miner, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(miner.getBattery()));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCFactoryBlockEntities.MINING_WELL,
                (miner, direction) -> EmptyResourceHandler.instance());

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCFactoryBlockEntities.PUMP,
                (pump, direction) -> pump.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCFactoryBlockEntities.PUMP,
                (pump, direction) -> pump.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCFactoryBlockEntities.PUMP,
                (pump, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(pump.getBattery()));

        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCFactoryBlockEntities.TANK,
                (tank, direction) -> new TankColumnResourceHandler(tank));
        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCFactoryBlockEntities.PUMP,
                (pump, direction) -> pumpFluidExtractOnly(pump));

        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCFactoryBlockEntities.FLOOD_GATE,
                (floodGate, direction) -> floodGate.getTank());

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCFactoryBlockEntities.CHUTE,
                (chute, direction) -> chute.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCFactoryBlockEntities.CHUTE,
                (chute, direction) -> chute.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCFactoryBlockEntities.CHUTE,
                (chute, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(chute.getBattery()));
        event.registerBlockEntity(Attachments.Item.BLOCK, BCFactoryBlockEntities.CHUTE,
                (chute, direction) -> chute.getItemHandler(direction));

        event.registerBlockEntity(MjAPI.CAP_RECEIVER, BCFactoryBlockEntities.DISTILLER,
                (distiller, direction) -> distiller.getMjReceiver());
        event.registerBlockEntity(MjAPI.CAP_CONNECTOR, BCFactoryBlockEntities.DISTILLER,
                (distiller, direction) -> distiller.getMjReceiver());
        event.registerBlockEntity(Attachments.Energy.BLOCK, BCFactoryBlockEntities.DISTILLER,
                (distiller, direction) -> MjBatteryEnergyHandler.createIfRfEnabled(distiller.getBattery()));
        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCFactoryBlockEntities.DISTILLER,
                (distiller, direction) -> distillerFluidHandler(distiller, direction));

        event.registerBlockEntity(Attachments.Fluid.BLOCK, BCFactoryBlockEntities.HEAT_EXCHANGE,
                (heatExchange, direction) -> heatExchangeFluidHandler(heatExchange, direction));
    }

    private static ResourceHandler<FluidResource> heatExchangeFluidHandler(
            buildcraft.factory.tile.TileHeatExchange heatExchange,
            net.minecraft.core.Direction direction) {
        buildcraft.lib.transfer.fluid.FluidStacksResourceHandler tank =
                heatExchange.getFluidTankForDirection(direction);
        if (tank == null) {
            return null;
        }
        if (tank instanceof buildcraft.factory.tile.TileHeatExchange.OutputTank) {
            return buildcraft.lib.transfer.fluid.SidedFluidHandlers.extractOnly(tank);
        }
        return buildcraft.lib.transfer.fluid.SidedFluidHandlers.insertOnly(tank);
    }

    private static ResourceHandler<FluidResource> pumpFluidExtractOnly(buildcraft.factory.tile.TilePump pump) {
        return buildcraft.lib.transfer.fluid.SidedFluidHandlers.extractOnly(pump.getTank());
    }

    private static ResourceHandler<FluidResource> distillerFluidHandler(
            buildcraft.factory.tile.TileDistiller_BC8 distiller,
            net.minecraft.core.Direction direction) {
        ResourceHandler<FluidResource> tank = distiller.getTankForSide(direction);
        if (tank == null) {
            return null;
        }
        if (tank == distiller.getTankIn()) {
            return buildcraft.lib.transfer.fluid.SidedFluidHandlers.insertOnly(tank);
        }
        return buildcraft.lib.transfer.fluid.SidedFluidHandlers.extractOnly(tank);
    }

    public static void onConfigReloaded() {
        TilePump.onConfigReloaded();
    }
}
