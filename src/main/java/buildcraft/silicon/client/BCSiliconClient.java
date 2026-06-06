package buildcraft.silicon.client;

import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.fabric.client.event.ClientPlayerNetworkEvent;
import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.client.fluid.FluidClientCache;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.FacadeItemModel;
import buildcraft.silicon.client.model.GateItemModel;
import buildcraft.silicon.client.model.LensItemModel;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.client.model.key.KeyPlugLens;
import buildcraft.silicon.client.model.key.KeyPlugSimple;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.client.model.plug.PlugBakerLens;
import buildcraft.silicon.client.model.plug.PlugBakerSimpleItems;
import buildcraft.silicon.client.model.plug.PlugGateBaker;
import buildcraft.silicon.client.render.PlugGateRenderer;
import buildcraft.silicon.client.render.PlugPulsarRenderer;
import buildcraft.silicon.plug.FacadeStateManager;
import buildcraft.silicon.plug.PluggableGate;
import buildcraft.silicon.plug.PluggablePulsar;
import buildcraft.transport.client.model.PipeModelCacheAll;
import java.util.Map;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCSiliconClient {
   private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft");
   private static Map<BlockState, BlockStateModel> cachedBlockStateModels;

   public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
      if (PipeApiClient.registry != null) {
         PipeApiClient.registry.registerBaker(KeyPlugGate.class, PlugGateBaker.INSTANCE);
         PipeApiClient.registry.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);
         PipeApiClient.registry.registerBaker(KeyPlugLens.class, PlugBakerLens.INSTANCE);
         PipeApiClient.registry.registerBaker(KeyPlugSimple.class, PlugBakerSimpleItems.INSTANCE);
         PipeApiClient.registry.registerRenderer(PluggablePulsar.class, PlugPulsarRenderer.INSTANCE);
         PipeApiClient.registry.registerRenderer(PluggableGate.class, PlugGateRenderer.INSTANCE);
      } else {
         LOGGER.warn("[silicon.client] PipeApiClient.registry is null at ModifyBakingResult! Facade in-world rendering will not work.");
      }

      Map<Identifier, ItemModel> itemModels = event.getBakingResult().itemStackModels();
      Identifier facadeId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_FACADE);
      ItemModel vanillaModel = itemModels.get(facadeId);
      if (vanillaModel != null) {
         itemModels.put(facadeId, new FacadeItemModel());
      }

      Identifier gateId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_GATE);
      ItemModel vanillaGateModel = itemModels.get(gateId);
      if (vanillaGateModel != null) {
         itemModels.put(gateId, new GateItemModel());
      }

      Identifier lensId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_LENS);
      ItemModel vanillaLensModel = itemModels.get(lensId);
      if (vanillaLensModel != null) {
         itemModels.put(lensId, new LensItemModel());
      }

      FacadeItemModel.onModelBake();
      GateItemModel.onModelBake();
      LensItemModel.onModelBake();
      PlugGateBaker.onModelBake();
      PlugGateRenderer.onModelBake();
      PipeModelCacheAll.clearAll();
      FluidClientCache.clear();
      PlugBakerSimpleItems.onModelBake();
      cachedBlockStateModels = event.getBakingResult().blockStateModels();
   }

   public static void runDeferredDedup() {
      if (cachedBlockStateModels != null) {
         FacadeDeduplicator.deduplicateVisuallyIdentical(cachedBlockStateModels);
         cachedBlockStateModels = null;
      }
   }

   public static final class GameBus {
      public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
         FacadeStateManager.ensureInitialized();
         BCSiliconClient.runDeferredDedup();
         FacadeDeduplicator.applyRedirectAuthority();
      }
   }
}
