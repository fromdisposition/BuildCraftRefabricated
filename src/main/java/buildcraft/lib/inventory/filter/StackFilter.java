package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import net.minecraft.world.item.ItemStack;

public enum StackFilter implements IStackFilter {
   ALL {
      @Override
      public boolean matches(ItemStack stack) {
         return true;
      }
   },
   NONE {
      @Override
      public boolean matches(ItemStack stack) {
         return false;
      }
   };
}
