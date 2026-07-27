/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.client;

import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.fabric.client.event.ClientPlayerNetworkEvent;
import buildcraft.fabric.client.event.ModelEvent;
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
//? if >= 1.21.10 {
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BCSiliconClient {
   private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft");
   //? if >= 1.21.10 {
   private static Map<BlockState, BlockStateModel> cachedBlockStateModels;
   //?}

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

      //? if >= 1.21.10 {
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
      PlugPulsarRenderer.onModelBake();
      PipeModelCacheAll.clearAll();
      PlugBakerSimpleItems.onModelBake();
      PlugBakerLens.onModelBake();
      cachedBlockStateModels = event.getBakingResult().blockStateModels();
      //?} else {
      /*java.util.Map<net.minecraft.client.resources.model.ModelIdentifier, net.minecraft.client.resources.model.BakedModel> models = event.getModels();
      Identifier facadeId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_FACADE);
      models.put(net.minecraft.client.resources.model.ModelIdentifier.inventory(facadeId), new FacadeItemModel());
      Identifier gateId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_GATE);
      models.put(net.minecraft.client.resources.model.ModelIdentifier.inventory(gateId), new GateItemModel());
      Identifier lensId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_LENS);
      models.put(net.minecraft.client.resources.model.ModelIdentifier.inventory(lensId), new LensItemModel());
      FacadeItemModel.onModelBake();
      GateItemModel.onModelBake();
      LensItemModel.onModelBake();
      PlugGateBaker.onModelBake();
      PlugGateRenderer.onModelBake();
      PlugPulsarRenderer.onModelBake();
      PipeModelCacheAll.clearAll();
      PlugBakerSimpleItems.onModelBake();
      PlugBakerLens.onModelBake();
      *///?}
   }

   public static void runDeferredDedup() {
      //? if >= 1.21.10 {
      if (cachedBlockStateModels != null) {
         FacadeDeduplicator.deduplicateVisuallyIdentical(cachedBlockStateModels);
         cachedBlockStateModels = null;
      }
      //?} else {
      /*FacadeDeduplicator.deduplicateVisuallyIdentical();
      *///?}
   }

   public static final class GameBus {
      public static void onClientLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
         FacadeStateManager.ensureInitialized();
         BCSiliconClient.runDeferredDedup();
         FacadeDeduplicator.applyRedirectAuthority();
      }
   }
}
