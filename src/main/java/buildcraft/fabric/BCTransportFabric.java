package buildcraft.fabric;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.lib.mj.MjBlockCapabilities;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportCreativeTabs;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.BCTransportRecipeSerializers;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.net.PipePayloadMessageQueue;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerEntityInteract;
import buildcraft.transport.stripes.StripesHandlerHoe;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraft.transport.stripes.StripesHandlerPipes;
import buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import buildcraft.transport.stripes.StripesHandlerPlant;
import buildcraft.transport.stripes.StripesHandlerShears;
import buildcraft.transport.stripes.StripesHandlerUse;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.SavedDataWireSystems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.server.level.ServerLevel;
import team.reborn.energy.api.EnergyStorage;

public final class BCTransportFabric {
   private BCTransportFabric() {
   }

   public static void register() {
      BCTransportConfig.ensureLoaded();
      BCTransportPipes.preInit();
      BCTransportPlugs.preInit();
      BCTransportStatements.preInit();
      BCTransportBlocks.register();
      BCTransportItems.register();
      BCTransportRecipeSerializers.register();
      BCTransportMenuTypes.register();
      BCTransportBlockEntities.register();
      BCTransportCreativeTabs.register();
      BCTransportAttachments.register();
      BCTransportConfig.registerPowerTransferData();
      BCTransportConfig.registerRfTransferData();
      BCTransportConfig.registerFluidTransferData();
      initStripesRegistry();
      registerMjCapabilities();
      registerNativeTransfer();
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         PipeItemMessageQueue.serverTick();
         PipePayloadMessageQueue.serverTick();

         for (ServerLevel serverLevel : server.getAllLevels()) {
            SavedDataWireSystems.get(serverLevel).tick();
         }
      });
   }

   private static void registerMjCapabilities() {
      MjBlockCapabilities.registerReceiver(BCTransportBlockEntities.PIPE_HOLDER, TilePipeHolder::getMjReceiverCapability);
      MjBlockCapabilities.registerRedstoneReceiver(BCTransportBlockEntities.PIPE_HOLDER, TilePipeHolder::getMjRedstoneReceiverCapability);
      MjBlockCapabilities.registerConnector(BCTransportBlockEntities.PIPE_HOLDER, TilePipeHolder::getMjConnectorCapability);
   }

   private static void registerNativeTransfer() {
      FluidStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedFluidStorage(side), BCTransportBlockEntities.PIPE_HOLDER);
      ItemStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedItemStorage(side), BCTransportBlockEntities.PIPE_HOLDER);
      EnergyStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedEnergyStorage(side), BCTransportBlockEntities.PIPE_HOLDER);
      ItemStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedItemStorage(side), BCTransportBlockEntities.FILTERED_BUFFER);
   }

   private static void initStripesRegistry() {
      PipeApi.stripeRegistry = StripesRegistry.INSTANCE;
      PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
      PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
      PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
      PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, EnumHandlerPriority.LOW);
      PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
      PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, EnumHandlerPriority.LOW);
      PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, EnumHandlerPriority.LOW);
      PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, EnumHandlerPriority.LOW);
      PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);
   }
}
