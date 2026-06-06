package buildcraft.lib.misc;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;

public class ItemStackKey {
   public static final ItemStackKey EMPTY = new ItemStackKey(ItemStack.EMPTY);
   @Nonnull
   public final ItemStack baseStack;
   private final int hash;

   public ItemStackKey(@Nonnull ItemStack stack) {
      if (stack.isEmpty()) {
         this.baseStack = ItemStack.EMPTY;
         this.hash = 0;
      } else {
         this.baseStack = stack.copy();
         this.hash = ItemStack.hashItemAndComponents(this.baseStack);
      }
   }

   @Override
   public int hashCode() {
      return this.hash;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      ItemStackKey other = (ItemStackKey)obj;
      return this.hash != other.hash ? false : ItemStack.isSameItemSameComponents(this.baseStack, other.baseStack);
   }

   @Override
   public String toString() {
      return "[ItemStackKey " + this.baseStack + "]";
   }
}
