package buildcraft.lib.recipe;

import buildcraft.lib.misc.ItemStackKey;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ChangingItemStack extends ChangingObject<ItemStackKey> {
   public ChangingItemStack(List<ItemStack> stacks) {
      super(makeStackArray(stacks.toArray(new ItemStack[0])));
   }

   public ChangingItemStack(ItemStack stack) {
      super(makeSingleStackArray(stack));
   }

   private static ItemStackKey[] makeSingleStackArray(ItemStack stack) {
      return stack.isEmpty() ? new ItemStackKey[]{ItemStackKey.EMPTY} : new ItemStackKey[]{new ItemStackKey(stack)};
   }

   private static ItemStackKey[] makeStackArray(ItemStack[] stacks) {
      if (stacks.length == 0) {
         return new ItemStackKey[]{ItemStackKey.EMPTY};
      }

      ItemStackKey[] arr = new ItemStackKey[stacks.length];

      for (int i = 0; i < stacks.length; i++) {
         arr[i] = new ItemStackKey(stacks[i]);
      }

      return arr;
   }

   public boolean matches(ItemStack target) {
      for (ItemStackKey s : this.options) {
         if (ItemStack.isSameItemSameComponents(s.baseStack, target)) {
            return true;
         }
      }

      return false;
   }
}
