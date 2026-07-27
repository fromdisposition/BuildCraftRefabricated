package buildcraft.transport.platform;

import buildcraft.api.core.EnumHandlerPriority;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.BCTransportAttachments;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportConfig;
import buildcraft.transport.BCTransportCreativeTabs;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.BCTransportPipes;
import buildcraft.transport.BCTransportPlugs;
import buildcraft.transport.BCTransportRfPipes;
import buildcraft.transport.BCTransportRecipeSerializers;
import buildcraft.transport.BCTransportStatements;
import buildcraft.transport.net.PipeItemMessageQueue;
import buildcraft.transport.net.PipePayloadMessageQueue;
import buildcraft.transport.pipe.StripesRegistry;
import buildcraft.transport.stripes.StripesHandlerDispenser;
import buildcraft.transport.stripes.StripesHandlerEntityInteract;
import buildcraft.transport.stripes.StripesHandlerHoe;
import buildcraft.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraft.transport.stripes.PipeExtensionManager;
import buildcraft.transport.stripes.StripesHandlerPipes;
import buildcraft.transport.stripes.StripesHandlerPlaceBlock;
import buildcraft.transport.stripes.StripesHandlerPlant;
import buildcraft.transport.stripes.StripesHandlerShears;
import buildcraft.transport.stripes.StripesHandlerUse;
import buildcraft.transport.tile.TilePipeHolder;
import buildcraft.transport.wire.SavedDataWireSystems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

public final class BCTransportFabric {
   private BCTransportFabric() {
   }

   public static void register() {
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
      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
         // Order matters: preInit() creates the RF PipeDefinition instances (BCTransportPipes.woodRf, ...);
         // registerRfTransferData() keys its transfer map on exactly those instances. If registration ran first
         // the defs would still be null, every put() would use a null key, and getRfTransferInfo() would fall
         // back to rfInfoDefault (80, isReceiver=false) — leaving the wooden RF pipe unable to receive any power.
         BCTransportRfPipes.preInit();
         BCTransportConfig.registerRfTransferData();
      }
      BCTransportConfig.registerFluidTransferData();
      initStripesRegistry();
      registerMjCapabilities();
      registerNativeTransfer();
      TilePipeHolder.registerGuiViewerDisconnectHook();
      ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TilePipeHolder pipeHolder) pipeHolder.onLoad();
      });
      ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> {
         if (blockEntity instanceof TilePipeHolder pipeHolder) pipeHolder.onChunkUnload();
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         PipeItemMessageQueue.serverTick();
         PipePayloadMessageQueue.serverTick();
         PipeExtensionManager.INSTANCE.tick(server);

         for (ServerLevel serverLevel : server.getAllLevels()) {
            SavedDataWireSystems.get(serverLevel).tick();
         }
      });
   }

   private static void registerMjCapabilities() {
      MjAPI.CAP_RECEIVER.registerForBlockEntity(TilePipeHolder::getMjReceiverCapability, BCTransportBlockEntities.PIPE_HOLDER);
      MjAPI.CAP_REDSTONE_RECEIVER.registerForBlockEntity(TilePipeHolder::getMjRedstoneReceiverCapability, BCTransportBlockEntities.PIPE_HOLDER);
      MjAPI.CAP_CONNECTOR.registerForBlockEntity(TilePipeHolder::getMjConnectorCapability, BCTransportBlockEntities.PIPE_HOLDER);
   }

   private static void registerNativeTransfer() {
      FluidStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedFluidStorage(side), BCTransportBlockEntities.PIPE_HOLDER);
      ItemStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedItemStorage(side), BCTransportBlockEntities.PIPE_HOLDER);
      ItemStorage.SIDED.registerForBlockEntity((tile, side) -> tile.getSidedItemStorage(side), BCTransportBlockEntities.FILTERED_BUFFER);
      if (FabricLoader.getInstance().isModLoaded("team_reborn_energy")) {
         BCTransportFabricTre.register();
      }
   }

   private static void initStripesRegistry() {
      PipeApi.extensionManager = PipeExtensionManager.INSTANCE;
      PipeExtensionManager.INSTANCE.registerRetractionPipe(BCTransportPipes.structure);
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
