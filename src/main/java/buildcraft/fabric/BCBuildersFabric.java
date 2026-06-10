package buildcraft.fabric;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.template.TemplateApi;
import buildcraft.builders.BCBuildersBlockEntities;
import buildcraft.builders.BCBuildersBlocks;
import buildcraft.builders.BCBuildersEntities;
import buildcraft.builders.BCBuildersEventDist;
import buildcraft.builders.BCBuildersItems;
import buildcraft.builders.BCBuildersMenuTypes;
import buildcraft.builders.BCBuildersSchematics;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.addon.AddonFillerPlanner;
import buildcraft.builders.registry.FillerRegistry;
import buildcraft.builders.snapshot.RulesLoader;
import buildcraft.builders.snapshot.TemplateHandlerDefault;
import buildcraft.builders.snapshot.TemplateRegistry;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileElectronicLibrary;
import buildcraft.builders.tile.TileFiller;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.marker.volume.AddonsRegistry;
import buildcraft.lib.fabric.transfer.AutoProvidingItemStorage;
import buildcraft.lib.mj.MjBlockCapabilities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.resources.Identifier;
import team.reborn.energy.api.EnergyStorage;

public final class BCBuildersFabric {
   private BCBuildersFabric() {
   }

   public static void onConfigReloaded() {
   }

   public static void register() {
      FillerManager.registry = FillerRegistry.INSTANCE;
      TemplateApi.templateRegistry = TemplateRegistry.INSTANCE;
      TemplateApi.templateRegistry.addHandler(TemplateHandlerDefault.INSTANCE);
      BCBuildersSchematics.preInit();
      BCBuildersStatements.preInit();
      BCBuildersBlocks.register();
      BCBuildersItems.register();
      BCBuildersBlockEntities.register();
      BCBuildersEntities.register();
      BCBuildersMenuTypes.register();
      AddonsRegistry.INSTANCE.register(Identifier.parse("buildcraftbuilders:filler_planner"), AddonFillerPlanner.class);
      RulesLoader.loadAll();
      registerMjCapabilities();
      registerNativeTransfer();
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> BCBuildersEventDist.INSTANCE.onServerTick());
   }

   private static void registerNativeTransfer() {
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileQuarry quarry ? quarry.getSidedEnergyStorage() : null, BCBuildersBlockEntities.QUARRY
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileQuarry ? AutoProvidingItemStorage.INSTANCE : null, BCBuildersBlockEntities.QUARRY
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileFiller filler ? filler.getSidedItemStorage(direction) : null, BCBuildersBlockEntities.FILLER
         );
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileFiller filler ? filler.getSidedEnergyStorage() : null, BCBuildersBlockEntities.FILLER
         );
      EnergyStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileBuilder builder ? builder.getSidedEnergyStorage() : null, BCBuildersBlockEntities.BUILDER
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileBuilder builder ? builder.getSidedItemStorage(direction) : null,
            BCBuildersBlockEntities.BUILDER
         );
      FluidStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileBuilder builder ? builder.getSidedFluidStorage(direction) : null,
            BCBuildersBlockEntities.BUILDER
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileElectronicLibrary library ? library.getSidedItemStorage(direction) : null,
            BCBuildersBlockEntities.LIBRARY
         );
      ItemStorage.SIDED
         .registerForBlockEntity(
            (blockEntity, direction) -> blockEntity instanceof TileArchitectTable architect ? architect.getSidedItemStorage(direction) : null,
            BCBuildersBlockEntities.ARCHITECT
         );
   }

   private static void registerMjCapabilities() {
      MjBlockCapabilities.registerReceiver(BCBuildersBlockEntities.QUARRY, (quarry, direction) -> quarry.getMjReceiver());
      MjBlockCapabilities.registerReceiver(BCBuildersBlockEntities.FILLER, (filler, direction) -> filler.getMjReceiver());
      MjBlockCapabilities.registerReceiver(BCBuildersBlockEntities.BUILDER, (builder, direction) -> builder.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCBuildersBlockEntities.QUARRY, (quarry, direction) -> quarry.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCBuildersBlockEntities.FILLER, (filler, direction) -> filler.getMjReceiver());
      MjBlockCapabilities.registerConnector(BCBuildersBlockEntities.BUILDER, (builder, direction) -> builder.getMjReceiver());
   }
}
