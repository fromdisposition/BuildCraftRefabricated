package buildcraft.fabric;

import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.debug.AdvDebugRenderer;
import buildcraft.lib.gui.config.GuiConfigManager;
import buildcraft.lib.misc.data.ModelVariableData;
import java.util.HashSet;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class BCLibFabricClient {
   private BCLibFabricClient() {
   }

   public static void init() {
      BCReloadFabric.initClient();
      AdvDebugRenderer.register();
      GuiConfigManager.init(FabricLoader.getInstance().getConfigDir().resolve("buildcraft").resolve("gui_state.json"));
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "fluid_heat_templates");
         }

         public void onResourceManagerReload(ResourceManager manager) {
            BcFluidTintUtil.reloadTemplates(manager);
         }
      });
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "models");
         }

         public void onResourceManagerReload(ResourceManager manager) {
            HashSet<Identifier> sprites = new HashSet<>();
            ModelHolderRegistry.onTextureStitchPre(sprites);
            ModelHolderRegistry.onModelBake();
            ModelVariableData.onModelBake();
         }
      });
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
         public Identifier getFabricId() {
            return Identifier.fromNamespaceAndPath("buildcraftlib", "guide");
         }

         public void onResourceManagerReload(ResourceManager manager) {
            GuideManager.INSTANCE.onResourceManagerReload(manager);
         }
      });
   }
}
