package buildcraft.silicon.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import buildcraft.fabric.event.EventPriority;
import buildcraft.fabric.event.SubscribeEvent;
import buildcraft.fabric.client.event.ModelEvent;
import buildcraft.fabric.client.event.RegisterMenuScreensEvent;

import buildcraft.api.transport.pipe.PipeApiClient;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.client.model.FacadeItemModel;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.silicon.client.model.key.KeyPlugGate;
import buildcraft.silicon.client.model.plug.PlugBakerFacade;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.plug.FacadeStateManager;

public class BCSiliconClient {
    private static final Logger LOGGER = LoggerFactory.getLogger("BuildCraft");

    private static java.util.Map<net.minecraft.world.level.block.state.BlockState,

            net.minecraft.client.renderer.block.dispatch.BlockStateModel> cachedBlockStateModels;

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {

        if (PipeApiClient.registry != null) {
            PipeApiClient.registry.registerBaker(KeyPlugGate.class, buildcraft.silicon.client.model.plug.PlugGateBaker.INSTANCE);
            PipeApiClient.registry.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);
            PipeApiClient.registry.registerBaker(buildcraft.silicon.client.model.key.KeyPlugLens.class, buildcraft.silicon.client.model.plug.PlugBakerLens.INSTANCE);
            PipeApiClient.registry.registerBaker(buildcraft.silicon.client.model.key.KeyPlugSimple.class, buildcraft.silicon.client.model.plug.PlugBakerSimpleItems.INSTANCE);

            PipeApiClient.registry.registerRenderer(buildcraft.silicon.plug.PluggablePulsar.class, buildcraft.silicon.client.render.PlugPulsarRenderer.INSTANCE);
            PipeApiClient.registry.registerRenderer(buildcraft.silicon.plug.PluggableGate.class, buildcraft.silicon.client.render.PlugGateRenderer.INSTANCE);
        } else {
            LOGGER.warn("[silicon.client] PipeApiClient.registry is null at ModifyBakingResult! "
                + "Facade in-world rendering will not work.");
        }

        var itemModels = event.getBakingResult().itemStackModels();
        Identifier facadeId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_FACADE.get());

        ItemModel vanillaModel = itemModels.get(facadeId);
        if (vanillaModel != null) {
            itemModels.put(facadeId, new FacadeItemModel());
        }

        Identifier gateId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_GATE.get());
        ItemModel vanillaGateModel = itemModels.get(gateId);
        if (vanillaGateModel != null) {
            itemModels.put(gateId, new buildcraft.silicon.client.model.GateItemModel());
        }

        Identifier lensId = BuiltInRegistries.ITEM.getKey(BCSiliconItems.PLUG_LENS.get());
        ItemModel vanillaLensModel = itemModels.get(lensId);
        if (vanillaLensModel != null) {
            itemModels.put(lensId, new buildcraft.silicon.client.model.LensItemModel());
        }

        FacadeItemModel.onModelBake();
        buildcraft.silicon.client.model.GateItemModel.onModelBake();
        buildcraft.silicon.client.model.LensItemModel.onModelBake();
        buildcraft.silicon.client.model.plug.PlugGateBaker.onModelBake();
        buildcraft.silicon.client.render.PlugGateRenderer.onModelBake();

        buildcraft.transport.client.model.PipeModelCacheAll.clearModels();
        buildcraft.silicon.client.model.plug.PlugBakerSimpleItems.onModelBake();

        cachedBlockStateModels = event.getBakingResult().blockStateModels();
    }

    public static void runDeferredDedup() {
        if (cachedBlockStateModels != null) {
            FacadeDeduplicator.deduplicateVisuallyIdentical(cachedBlockStateModels);
            cachedBlockStateModels = null;
        }
    }

    public static final class GameBus {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onClientLoggingIn(buildcraft.fabric.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
            FacadeStateManager.ensureInitialized();
            runDeferredDedup();
            FacadeDeduplicator.applyRedirectAuthority();
        }
    }

    /** @deprecated Fabric client wiring is in {@link buildcraft.fabric.BCSiliconFabricClient}. */
    @Deprecated
    public static void initClient() {
    }
}

