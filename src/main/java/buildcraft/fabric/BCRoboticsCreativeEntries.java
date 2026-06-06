package buildcraft.fabric;

import buildcraft.robotics.BCRoboticsItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab.Output;

public final class BCRoboticsCreativeEntries {
   private BCRoboticsCreativeEntries() {
   }

   public static void addMainTabItems(Output output) {
      accept(output, BCRoboticsItems.ZONE_PLANNER);
   }

   private static void accept(Output output, Item item) {
      if (item != null) {
         output.accept(item);
      }
   }
}
