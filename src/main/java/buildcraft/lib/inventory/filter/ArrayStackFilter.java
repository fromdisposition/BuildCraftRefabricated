package buildcraft.lib.inventory.filter;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.recipes.StackDefinition;
import buildcraft.lib.misc.StackUtil;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ArrayStackFilter implements IStackFilter {
   protected NonNullList<ItemStack> stacks;

   public ArrayStackFilter(ItemStack... stacks) {
      this.stacks = StackUtil.listOf(stacks);
   }

   @Override
   public boolean matches(@Nonnull ItemStack stack) {
      if (this.stacks.size() != 0 && this.hasFilter()) {
         for (ItemStack s : this.stacks) {
            if (StackUtil.isMatchingItem(s, stack)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   public boolean matches(IStackFilter filter2) {
      for (ItemStack s : this.stacks) {
         if (filter2.matches(s)) {
            return true;
         }
      }

      return false;
   }

   public NonNullList<ItemStack> getStacks() {
      return this.stacks;
   }

   public boolean hasFilter() {
      for (ItemStack filter : this.stacks) {
         if (!filter.isEmpty()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public NonNullList<ItemStack> getExamples() {
      return this.stacks;
   }

   public static StackDefinition definition(int count, ItemStack... stacks) {
      return new StackDefinition(new ArrayStackFilter(stacks), count);
   }

   public static StackDefinition definition(ItemStack... stacks) {
      return definition(1, stacks);
   }

   public static StackDefinition definition(int count, Block... blocks) {
      return definition(count, Arrays.stream(blocks).map(ItemStack::new).toArray(ItemStack[]::new));
   }

   public static StackDefinition definition(Block... blocks) {
      return definition(1, blocks);
   }

   public static StackDefinition definition(int count, Item... items) {
      return definition(count, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
   }

   public static StackDefinition definition(Item... items) {
      return definition(1, items);
   }
}
