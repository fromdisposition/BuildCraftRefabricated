package buildcraft.fabric;

import buildcraft.core.BCUnifiedClientConfig;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.fluid.BcFluidAppearanceCache;
import buildcraft.lib.client.fluid.BcFluidFogProfiles;
import buildcraft.lib.client.fluid.FluidDisplayNamesClient;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
//? if >= 1.21.10 {
import buildcraft.lib.client.model.VariableModelDeserializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
//?}
import buildcraft.lib.debug.AdvDebugRenderer;
import buildcraft.lib.fabric.loader.GamePaths;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.misc.data.ModelVariableData;
import java.util.HashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >= 26.1 {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
//?} else {
/*import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
*///?}
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
//? if >= 26.1 {
import net.minecraft.server.packs.resources.PreparableReloadListener;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

public final class BCLibFabricClient {
   private static boolean lastAutoHighContrast;

   private BCLibFabricClient() {
   }

   // 1.21.x uses the (still-functional) resource-loader-v0 API; it is deprecated there in favour of
   // the async resource.v1 reloader, but v0 is sufficient for these synchronous cache-clearing reloads.
   @SuppressWarnings("deprecation")
   public static void init() {
      //? if >= 1.21.10 {
      UnbakedModelDeserializer.register(VariableModelDeserializer.TYPE_ID, VariableModelDeserializer.INSTANCE);
      //?}
      // On 1.21.1 there is no UnbakedModelDeserializer API; the "buildcraftlib:variable" engine models are
      // instead kept out of vanilla's bulk model parse by BlockModelVariableSkipMixin (the engine BER draws
      // the real model via ModelHolderVariable), so nothing needs registering here.
      FluidDisplayNamesClient.register();
      BcFluidFogProfiles.loadFromClasspath();
      BCReloadFabric.initClient();
      registerColorBlindAutoWatcher();
      AdvDebugRenderer.register();
      GuiConfigManager.init(GamePaths.BUILDCRAFT_CONFIG_DIR.resolve("buildcraftrefabricated-gui-state.json"));
      //? if >= 26.1 {
      ResourceLoader clientResources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
      clientResources.registerReloadListener(
         Identifier.fromNamespaceAndPath("buildcraftlib", "fluid_client_profiles"),
         new SimpleReloadListener<Void>() {
            @Override
            protected Void prepare(PreparableReloadListener.SharedState state) {
               return null;
            }

            @Override
            protected void apply(Void prepared, PreparableReloadListener.SharedState state) {
               BcFluidFogProfiles.reload(state.resourceManager());
               BcFluidAppearanceCache.clear();
            }
         }
      );
      clientResources.registerReloadListener(
         Identifier.fromNamespaceAndPath("buildcraftlib", "models"),
         new SimpleReloadListener<Void>() {
            @Override
            protected Void prepare(PreparableReloadListener.SharedState state) {
               return null;
            }

            @Override
            protected void apply(Void prepared, PreparableReloadListener.SharedState state) {
               HashSet<Identifier> sprites = new HashSet<>();
               ModelHolderRegistry.onTextureStitchPre(sprites);
               ModelHolderRegistry.onModelBake();
               ModelVariableData.onModelBake();
            }
         }
      );
      clientResources.registerReloadListener(
         Identifier.fromNamespaceAndPath("buildcraftlib", "guide"),
         new SimpleReloadListener<ResourceManager>() {
            @Override
            protected ResourceManager prepare(PreparableReloadListener.SharedState state) {
               return state.resourceManager();
            }

            @Override
            protected void apply(ResourceManager manager, PreparableReloadListener.SharedState state) {
               GuideManager.INSTANCE.onResourceManagerReload(manager);
            }
         }
      );
      //?} else {
      /*// 1.21.x uses the synchronous ResourceManagerHelper API; BC's listeners only clear caches /
      // re-read from the manager, so a synchronous reload is sufficient.
      ResourceManagerHelper clientResources = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
      clientResources.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "fluid_client_profiles");
         }

         @Override
         public void onResourceManagerReload(ResourceManager manager) {
            BcFluidFogProfiles.reload(manager);
            BcFluidAppearanceCache.clear();
         }
      });
      clientResources.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "models");
         }

         @Override
         public void onResourceManagerReload(ResourceManager manager) {
            HashSet<Identifier> sprites = new HashSet<>();
            ModelHolderRegistry.onTextureStitchPre(sprites);
            ModelHolderRegistry.onModelBake();
            ModelVariableData.onModelBake();
         }
      });
      clientResources.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         @Override
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "guide");
         }

         @Override
         public void onResourceManagerReload(ResourceManager manager) {
            GuideManager.INSTANCE.onResourceManagerReload(manager);
         }
      });
      *///?}
   }

   private static void registerColorBlindAutoWatcher() {
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         if (BCLibConfig.colorBlindMode.get() != BCLibConfig.ColorBlindMode.AUTO) {
            return;
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc.options == null) {
            return;
         }

         boolean highContrast = mc.options.highContrast().get();
         if (highContrast != lastAutoHighContrast) {
            lastAutoHighContrast = highContrast;
            BCUnifiedClientConfig.onDisplayConfigReloaded();
         }
      });
   }
}
