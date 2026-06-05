package buildcraft.silicon;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import buildcraft.fabric.BCRegistries;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadeStateManager;

public final class BCSiliconCreativeTabs {
    public static CreativeModeTab FACADE_TAB;

    public static final ResourceKey<CreativeModeTab> FACADE_TAB_KEY =
            ResourceKey.create(
                    net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB,
                    BCRegistries.id(BCSilicon.MODID, "facades"));

    private BCSiliconCreativeTabs() {}

    public static void register() {
        FACADE_TAB = net.minecraft.core.Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BCRegistries.id(BCSilicon.MODID, "facades"),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 4)
                        .title(Component.translatable("itemGroup.buildcraft.facades"))
                        .icon(() -> {
                            FacadeBlockStateInfo preview = FacadeStateManager.previewState;
                            if (preview != null && preview != FacadeStateManager.defaultState) {
                                return BCSiliconItems.PLUG_FACADE.get()
                                        .createItemStack(FacadeInstance.createSingle(preview, false));
                            }
                            return BCSiliconItems.PLUG_FACADE.get().getDefaultInstance();
                        })
                        .displayItems((parameters, output) -> addFacadeItems(output))
                        .build());
    }

    private static void addFacadeItems(CreativeModeTab.Output output) {
        FacadeStateManager.ensureInitialized();
        buildcraft.silicon.client.BCSiliconClient.runDeferredDedup();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                output.accept(BCSiliconItems.PLUG_FACADE.get()
                        .createItemStack(FacadeInstance.createSingle(info, false)));
                output.accept(BCSiliconItems.PLUG_FACADE.get()
                        .createItemStack(FacadeInstance.createSingle(info, true)));
            }
        }
    }
}
