package buildcraft.transport.client;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import buildcraft.fabric.event.SubscribeEvent;
import buildcraft.fabric.client.event.EntityRenderersEvent;
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.fabric.client.event.RegisterColorHandlersEvent;
import buildcraft.fabric.client.event.RegisterMenuScreensEvent;
import buildcraft.fabric.client.block.ClientBlockExtensionsRegistry;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.IPluggableStaticBaker;

import buildcraft.lib.client.model.ModelHolderStatic;
import buildcraft.transport.client.model.plug.PlugBakerSimple;

import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.client.model.PipeBlockStateModel;
import buildcraft.transport.client.model.PipeItemModel;
import buildcraft.transport.client.render.PipeFlowRendererFluids;
import buildcraft.transport.client.render.PipeFlowRendererPower;
import buildcraft.transport.client.render.PipeFlowRendererFE;
import buildcraft.transport.client.render.PipePlacementHighlight;
import buildcraft.transport.client.render.RenderPipeHolder;
import buildcraft.transport.client.render.PipeBehaviourRendererStripes;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraft.transport.pipe.flow.PipeFlowFluids;
import buildcraft.transport.pipe.flow.PipeFlowItems;
import buildcraft.transport.pipe.flow.PipeFlowPower;
import buildcraft.transport.pipe.flow.PipeFlowRedstoneFlux;

public class BCTransportClient {

    public static final ModelHolderStatic BLOCKER = new ModelHolderStatic("buildcrafttransport:models/plugs/blocker.json");
    public static final ModelHolderStatic POWER_ADAPTER = new ModelHolderStatic("buildcrafttransport:models/plugs/power_adapter.json");

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER =
        new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR =
        new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

    public static void init() {
    }

    /** @deprecated Fabric client wiring is in {@link buildcraft.fabric.BCTransportFabricClient}. */
    @Deprecated
    public static void initClient() {
        init();
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {

    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        PipeApiClient.registry = PipeRegistryClient.INSTANCE;

        registerFlowRenderers();
    }

    @SubscribeEvent
    public static void registerClientExtensions(ClientBlockExtensionsRegistry event) {
        event.registerBlock(PipeHolderClientExtensions.INSTANCE, BCTransportBlocks.PIPE_HOLDER.get());
    }

    @SubscribeEvent
    public static void registerItemTintSources(buildcraft.fabric.client.event.RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(
                Identifier.fromNamespaceAndPath(BCTransport.MODID, "pipe_colour"),
                PipeColourTintSource.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {

        BlockState pipeState = BCTransportBlocks.PIPE_HOLDER.get().defaultBlockState();
        var blockModels = event.getBakingResult().blockStateModels();
        BlockStateModel vanillaModel = blockModels.get(pipeState);
        if (vanillaModel != null) {
            blockModels.put(pipeState, new PipeBlockStateModel(vanillaModel));
        }

        var itemModels = event.getBakingResult().itemStackModels();
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            Item pipeItem = (Item) PipeApi.pipeRegistry.getItemForPipe(def);
            if (pipeItem != null) {
                Identifier itemId = BuiltInRegistries.ITEM.getKey(pipeItem);
                ItemModel vanillaItemModel = itemModels.get(itemId);

                if (vanillaItemModel != null) {
                    itemModels.put(itemId, new PipeItemModel(vanillaItemModel, def));
                }
            }
        }
    }

    public static void registerFlowRenderers() {
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowRedstoneFlux.class, PipeFlowRendererFE.INSTANCE);
        PipeRegistryClient.INSTANCE.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);

        PipeRegistryClient.INSTANCE.registerRenderer(
            buildcraft.transport.pipe.behaviour.PipeBehaviourStripes.class,
            PipeBehaviourRendererStripes.INSTANCE
        );

        PipeRegistryClient.INSTANCE.registerBaker(KeyPlugBlocker.class, BAKER_PLUG_BLOCKER);
        PipeRegistryClient.INSTANCE.registerBaker(KeyPlugPowerAdaptor.class, BAKER_PLUG_POWER_ADAPTOR);
    }
}


