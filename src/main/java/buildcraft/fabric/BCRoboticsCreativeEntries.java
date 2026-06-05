package buildcraft.fabric;

import net.minecraft.world.item.CreativeModeTab;

import buildcraft.robotics.BCRoboticsItems;
import buildcraft.fabric.registry.DeferredItem;

public final class BCRoboticsCreativeEntries {
    private BCRoboticsCreativeEntries() {}

    public static void addMainTabItems(CreativeModeTab.Output output) {
        accept(output, BCRoboticsItems.ZONE_PLANNER);
    }

    private static void accept(CreativeModeTab.Output output, DeferredItem<?> item) {
        if (item == null) {
            return;
        }
        try {
            output.accept(item.get());
        } catch (IllegalStateException ignored) {
        }
    }
}
