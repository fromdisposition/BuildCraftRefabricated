package buildcraft.fabric;

import buildcraft.builders.BCBuildersItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab.Output;

public final class BCBuildersCreativeEntries {
   private BCBuildersCreativeEntries() {
   }

   public static void addMainTabItems(Output output) {
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

   private static void accept(Output output, Item item) {
      if (item != null) {
         output.accept(item);
      }
   }
}
