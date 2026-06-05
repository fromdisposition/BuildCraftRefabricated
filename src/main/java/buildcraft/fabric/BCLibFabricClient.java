package buildcraft.fabric;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import buildcraft.lib.client.fluid.BcFluidTintUtil;
import buildcraft.lib.client.model.ModelHolderRegistry;
import buildcraft.lib.misc.data.ModelVariableData;

public final class BCLibFabricClient {
    private BCLibFabricClient() {}

    public static void init() {
        BCReloadFabric.initClient();
        buildcraft.lib.debug.AdvDebugRenderer.register();

        buildcraft.lib.gui.config.GuiConfigManager.init(
                net.fabricmc.loader.api.FabricLoader.getInstance()
                        .getConfigDir()
                        .resolve("buildcraft")
                        .resolve("gui_state.json"));

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath("buildcraftlib", "fluid_heat_templates");
                    }

                    @Override
                    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
                        BcFluidTintUtil.reloadTemplates(manager);
                    }
                });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath("buildcraftlib", "models");
                    }

                    @Override
                    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
                        java.util.HashSet<Identifier> sprites = new java.util.HashSet<>();
                        ModelHolderRegistry.onTextureStitchPre(sprites);
                        ModelHolderRegistry.onModelBake();
                        ModelVariableData.onModelBake();
                    }
                });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(
                new net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener() {
                    @Override
                    public Identifier getFabricId() {
                        return Identifier.fromNamespaceAndPath("buildcraftlib", "guide");
                    }

                    @Override
                    public void onResourceManagerReload(net.minecraft.server.packs.resources.ResourceManager manager) {
                        buildcraft.lib.client.guide.GuideManager.INSTANCE.onResourceManagerReload(manager);
                    }
                });
    }
}
