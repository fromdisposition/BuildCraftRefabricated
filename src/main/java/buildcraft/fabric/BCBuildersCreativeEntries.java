package buildcraft.fabric;

import net.minecraft.world.item.CreativeModeTab;

import buildcraft.builders.BCBuildersItems;
import buildcraft.fabric.registry.DeferredItem;

public final class BCBuildersCreativeEntries {
    private BCBuildersCreativeEntries() {}

    public static void addMainTabItems(CreativeModeTab.Output output) {
        accept(output, BCBuildersItems.QUARRY);
        accept(output, BCBuildersItems.FILLER);
        accept(output, BCBuildersItems.BUILDER);
        accept(output, BCBuildersItems.ARCHITECT);
        accept(output, BCBuildersItems.LIBRARY);
        accept(output, BCBuildersItems.REPLACER);
        accept(output, BCBuildersItems.FRAME);
        accept(output, BCBuildersItems.BLUEPRINT_CLEAN);
        accept(output, BCBuildersItems.BLUEPRINT_USED);
        accept(output, BCBuildersItems.TEMPLATE_CLEAN);
        accept(output, BCBuildersItems.TEMPLATE_USED);
        accept(output, BCBuildersItems.SCHEMATIC_SINGLE_CLEAN);
        accept(output, BCBuildersItems.SCHEMATIC_SINGLE_USED);
        accept(output, BCBuildersItems.FILLER_PLANNER);
    }

    private static void accept(CreativeModeTab.Output output, DeferredItem<?> item) {
        if (item == null) {
            return;
        }
        try {
            output.accept(item.get());
        } catch (IllegalStateException ignored) {
            // Module items not bound yet — should not happen after mod init.
        }
    }
}
