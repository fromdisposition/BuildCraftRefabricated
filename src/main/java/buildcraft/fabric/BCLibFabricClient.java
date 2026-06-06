package buildcraft.fabric;

import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.debug.AdvDebugRenderer;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.misc.data.ModelVariableData;
import java.util.HashSet;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

public final class BCLibFabricClient {
   private BCLibFabricClient() {
   }

   public static void init() {
      BCReloadFabric.initClient();
      AdvDebugRenderer.register();
      GuiConfigManager.init(FabricLoader.getInstance().getConfigDir().resolve("buildcraft").resolve("gui_state.json"));
      ResourceLoader clientResources = ResourceLoader.get(PackType.CLIENT_RESOURCES);
      clientResources.registerReloadListener(
         Identifier.fromNamespaceAndPath("buildcraftlib", "fluid_heat_templates"),
         new SimpleReloadListener<Void>() {
            @Override
            protected Void prepare(PreparableReloadListener.SharedState state) {
               return null;
            }

            @Override
            protected void apply(Void prepared, PreparableReloadListener.SharedState state) {
               BcFluidTintUtil.reloadTemplates(state.resourceManager());
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
   }
}
