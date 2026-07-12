package buildcraft.fabric;

import buildcraft.api.registry.BuildCraftRegistryManager;
import buildcraft.api.registry.EventBuildCraftReload;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.GuidePageRegistry;
import buildcraft.lib.guide.GuideBookRegistry;
import buildcraft.lib.script.ReloadableRegistryManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.EndDataPackReload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;

public final class BCReloadFabric {
   private static boolean commonInit;
   private static boolean clientInit;

   private BCReloadFabric() {
   }

   public static void initCommon() {
      if (!commonInit) {
         commonInit = true;
         BuildCraftRegistryManager.managerDataPacks = ReloadableRegistryManager.DATA_PACKS;
         // The guidebook is client-only (GuideManager references RenderPipelines/Minecraft/TextureAtlasSprite).
         // Register its reload hook only on the client so the class never loads on a dedicated server.
         if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            EventBuildCraftReload.onFinishLoad(GuideManager.INSTANCE::onRegistryReload);
         }
         touchDataRegistries();
         ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)server -> {
            buildcraft.energy.tile.TileEngineIron_BC8.invalidateRecipeCaches();
            ReloadableRegistryManager.DATA_PACKS.reloadAll();
         });
         ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((EndDataPackReload)(server, resourceManager, success) -> {
            if (success) {
               buildcraft.energy.tile.TileEngineIron_BC8.invalidateRecipeCaches();
               ReloadableRegistryManager.DATA_PACKS.reloadAll();
            }
         });
      }
   }

   public static void initClient() {
      if (!clientInit) {
         clientInit = true;
         BuildCraftRegistryManager.managerResourcePacks = ReloadableRegistryManager.RESOURCE_PACKS;
         touchResourceRegistries();
         ReloadableRegistryManager.loadAll();
      }
   }

   private static void touchDataRegistries() {
      GuideBookRegistry ignored = GuideBookRegistry.INSTANCE;
   }

   private static void touchResourceRegistries() {
      GuidePageRegistry ignored = GuidePageRegistry.INSTANCE;
   }
}
