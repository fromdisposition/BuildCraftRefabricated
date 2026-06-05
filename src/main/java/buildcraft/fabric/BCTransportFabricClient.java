package buildcraft.fabric;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.fabric.client.event.ExtractBlockOutlineRenderStateEvent;
import buildcraft.fabric.client.block.ClientBlockExtensionsRegistry;
import buildcraft.lib.fabric.client.FabricModelModifyHooks;
import buildcraft.transport.BCTransportBlockEntities;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportMenuTypes;
import buildcraft.transport.client.BCTransportClient;
import buildcraft.transport.client.PipeBlockColourHandler;
import buildcraft.transport.client.PipeRegistryClient;
import buildcraft.transport.gui.GuiDiamondPipe;
import buildcraft.transport.gui.GuiDiamondWoodPipe;
import buildcraft.transport.gui.GuiEmzuliPipe_BC8;
import buildcraft.transport.gui.GuiFilteredBuffer;
import buildcraft.transport.client.render.PipePlacementHighlight;
import buildcraft.transport.client.render.RenderPipeHolder;

public final class BCTransportFabricClient {
    private BCTransportFabricClient() {}

    public static void init() {
        BCTransportClient.init();
        FabricModelModifyHooks.register(BCTransportClient::onModifyBakingResult);

        MenuScreens.register(BCTransportMenuTypes.FILTERED_BUFFER, GuiFilteredBuffer::new);
        MenuScreens.register(BCTransportMenuTypes.DIAMOND_PIPE, GuiDiamondPipe::new);
        MenuScreens.register(BCTransportMenuTypes.DIAMOND_WOOD_PIPE, GuiDiamondWoodPipe::new);
        MenuScreens.register(BCTransportMenuTypes.EMZULI_PIPE, GuiEmzuliPipe_BC8::new);

        BlockEntityRenderers.register(BCTransportBlockEntities.PIPE_HOLDER, RenderPipeHolder::new);
        PipeApiClient.registry = PipeRegistryClient.INSTANCE;
        BCTransportClient.registerFlowRenderers();

        ExtractBlockOutlineRenderStateEvent.register(PipePlacementHighlight::onExtractBlockOutline);

        registerBlockTintSources();
        registerClientExtensions();
    }

    private static void registerClientExtensions() {
        BCTransportClient.registerClientExtensions(new ClientBlockExtensionsRegistry());
    }

    private static void registerBlockTintSources() {
        PipeBlockColourHandler.onRegisterBlockTintSources(
                new buildcraft.fabric.client.event.RegisterColorHandlersEvent.BlockTintSources());
    }
}


