package buildcraft.lib.inventory;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.misc.StackUtil;
import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public enum AutomaticProvidingTransactor implements IItemTransactor.IItemExtractable {
   INSTANCE;

   @Nonnull
   @Override
   public ItemStack extract(IStackFilter filter, int min, int max, boolean simulate) {
      return StackUtil.EMPTY;
   }
}
