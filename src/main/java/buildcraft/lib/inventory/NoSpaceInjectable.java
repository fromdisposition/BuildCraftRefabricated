package buildcraft.lib.inventory;

import buildcraft.api.transport.IInjectable;
import javax.annotation.Nonnull;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public enum NoSpaceInjectable implements IInjectable {
   INSTANCE;

   @Override
   public boolean canInjectItems(Direction from) {
      return false;
   }

   @Nonnull
   @Override
   public ItemStack injectItem(@Nonnull ItemStack stack, boolean doAdd, Direction from, DyeColor color, double speed) {
      return stack;
   }
}
