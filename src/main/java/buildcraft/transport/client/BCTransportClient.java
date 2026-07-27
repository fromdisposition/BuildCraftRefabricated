/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.client;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;
import buildcraft.fabric.client.block.ClientBlockExtensionsRegistry;
import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.client.model.PipeBlockStateModel;
import buildcraft.transport.client.model.PipeItemModel;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraft.transport.client.model.plug.PlugBakerSimple;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.render.PipeFlowRendererEnergyAdapter;
import buildcraft.transport.client.render.PipeFlowRendererFluids;
import buildcraft.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;
import java.util.Map;
//? if >= 1.21.10 {
import net.minecraft.client.renderer.item.ItemModel;
//?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class BCTransportClient {
   public static final ModelHolderStatic BLOCKER = new ModelHolderStatic("buildcrafttransport:models/plugs/blocker.json");
   public static final ModelHolderStatic POWER_ADAPTER = new ModelHolderStatic("buildcrafttransport:models/plugs/power_adapter.json");
   public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER = new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
   public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR = new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

   public static void registerClientExtensions(ClientBlockExtensionsRegistry event) {
      event.registerBlock(PipeHolderClientExtensions.INSTANCE, BCTransportBlocks.PIPE_HOLDER);
   }

   //? if >= 1.21.10 {
   /**
    * Wraps the pipe block model via the native model-loading hook. This must NOT go through
    * {@code onModifyBakingResult}: that event mutates {@code BakingResult.blockStateModels()}, but on 26.x
    * ModelManager.loadModels copies that map into the ReloadState dispatch (createBlockStateToModelDispatch)
    * before apply() ever runs, so a swap made there is silently dropped. AfterBakeBlock wraps the model at bake
    * time, before any copy, on every version with the BlockStateModel pipeline.
    */
   public static void registerModelLoadingPlugin() {
      net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(
         pluginContext -> pluginContext.modifyBlockModelAfterBake()
            .register((model, context) -> context.state().getBlock() == BCTransportBlocks.PIPE_HOLDER ? new PipeBlockStateModel(model) : model)
      );
   }
   //?}

   public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
      //? if >= 1.21.10 {
      Map<Identifier, ItemModel> itemModels = event.getBakingResult().itemStackModels();

      for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
         Item pipeItem = (Item)PipeApi.pipeRegistry.getItemForPipe(def);
         if (pipeItem != null) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(pipeItem);
            ItemModel vanillaItemModel = itemModels.get(itemId);
            if (vanillaItemModel != null) {
               itemModels.put(itemId, new PipeItemModel(vanillaItemModel, def));
            }
         }
      }
      //?}
   }

   public static void registerFlowRenderers() {
      PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowPower.class, PipeFlowRendererEnergyAdapter.POWER);
      PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowRedstoneFlux.class, PipeFlowRendererEnergyAdapter.FE);
      PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
      PipeRegistryClient.INSTANCE.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);
      PipeRegistryClient.INSTANCE.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
      PipeRegistryClient.INSTANCE.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);
   }
}
