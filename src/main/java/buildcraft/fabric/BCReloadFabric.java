package buildcraft.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import buildcraft.api.registry.BuildCraftRegistryManager;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.script.ReloadableRegistryManager;

public final class BCReloadFabric {
    private static boolean commonInit;
    private static boolean clientInit;

    private BCReloadFabric() {}

    public static void initCommon() {
        if (commonInit) {
            return;
        }
        commonInit = true;

        BuildCraftRegistryManager.managerDataPacks = ReloadableRegistryManager.DATA_PACKS;
        EventBuildCraftReload.onFinishLoad(GuideManager.INSTANCE::onRegistryReload);

        touchDataRegistries();

        ServerLifecycleEvents.SERVER_STARTED.register(server ->
                ReloadableRegistryManager.DATA_PACKS.reloadAll());

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            if (success) {
                ReloadableRegistryManager.DATA_PACKS.reloadAll();
            }
        });
    }

    public static void initClient() {
        if (clientInit) {
            return;
        }
        clientInit = true;

        BuildCraftRegistryManager.managerResourcePacks = ReloadableRegistryManager.RESOURCE_PACKS;
        touchResourceRegistries();
        ReloadableRegistryManager.loadAll();
    }

    private static void touchDataRegistries() {
        var ignored = GuideBookRegistry.INSTANCE;
    }

    private static void touchResourceRegistries() {
        var ignored = GuidePageRegistry.INSTANCE;
    }
}


