package buildcraft.lib.fabric.transfer;

import buildcraft.api.items.IList;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class TriggerItemChecks {
   private TriggerItemChecks() {
   }

   public static boolean hasViews(@Nullable Storage<ItemVariant> storage) {
      if (storage == null) {
         return false;
      } else {
         Iterator var1 = storage.iterator();
         if (var1.hasNext()) {
            StorageView<ItemVariant> ignored = (StorageView<ItemVariant>)var1.next();
            return true;
         } else {
            return false;
         }
      }
   }

   public static TriggerItemChecks.InventoryScan scan(@Nullable Storage<ItemVariant> storage, ItemStack searchedStack) {
      TriggerItemChecks.InventoryScan result = new TriggerItemChecks.InventoryScan();
      if (storage == null) {
         return result;
      }

      boolean isList = !searchedStack.isEmpty() && searchedStack.getItem() instanceof IList;
      IList listFilter = isList ? (IList)searchedStack.getItem() : null;

      for (StorageView<ItemVariant> view : storage) {
         result.hasSlots = true;
         ItemStack stack = view.isResourceBlank() ? ItemStack.EMPTY : ((ItemVariant)view.getResource()).toStack(saturate(view.getAmount()));
         boolean stackMatchesSearch = matchesSearch(searchedStack, stack, isList, listFilter);
         result.foundItems = result.foundItems | (!stack.isEmpty() && stackMatchesSearch);
         result.foundSpace = result.foundSpace | hasSpace(view, stack, searchedStack, isList, listFilter, stackMatchesSearch);
      }

      return result;
   }

   public static float fillRatio(@Nullable Storage<ItemVariant> storage, ItemStack searchStack) {
      if (storage == null) {
         return 0.0F;
      }

      int itemSpace = 0;
      int foundItems = 0;

      for (StorageView<ItemVariant> view : storage) {
         ItemStack stackInSlot = view.isResourceBlank() ? ItemStack.EMPTY : ((ItemVariant)view.getResource()).toStack(saturate(view.getAmount()));
         int slotCapacity = saturate(view.getCapacity());
         if (stackInSlot.isEmpty()) {
            if (searchStack.isEmpty()) {
               itemSpace += slotCapacity;
            } else if (!(searchStack.getItem() instanceof IList)) {
               int count = Math.min(slotCapacity, searchStack.getMaxStackSize());
               itemSpace += count;
            }
         } else if (searchStack.isEmpty() || matchesStackOrList(searchStack, stackInSlot)) {
            itemSpace += Math.min(stackInSlot.getMaxStackSize(), slotCapacity);
            foundItems += stackInSlot.getCount();
         }
      }

      return itemSpace > 0 ? (float)foundItems / itemSpace : 0.0F;
   }

   private static boolean hasSpace(
      StorageView<ItemVariant> view, ItemStack stack, ItemStack searchedStack, boolean isList, @Nullable IList listFilter, boolean stackMatchesSearch
   ) {
      if (stack.isEmpty()) {
         return true;
      } else if (searchedStack.isEmpty()) {
         return stack.getCount() < stack.getMaxStackSize();
      } else if (isList) {
         return stackMatchesSearch && stack.getCount() < stack.getMaxStackSize();
      } else if (canStacksMerge(stack, searchedStack) && stack.getCount() < stack.getMaxStackSize()) {
         int amount = Math.min(searchedStack.getCount(), stack.getMaxStackSize() - stack.getCount());
         return amount > 0 && saturate(view.getCapacity()) >= stack.getCount() + amount;
      } else {
         return false;
      }
   }

   private static boolean matchesSearch(ItemStack searchedStack, ItemStack stack, boolean isList, @Nullable IList listFilter) {
      if (searchedStack.isEmpty()) {
         return true;
      } else {
         return !isList ? canStacksMerge(stack, searchedStack) : !stack.isEmpty() && listFilter != null && listFilter.matches(searchedStack, stack);
      }
   }

   private static boolean matchesStackOrList(ItemStack filter, ItemStack target) {
      if (!filter.isEmpty() && !target.isEmpty()) {
         return filter.getItem() instanceof IList list ? list.matches(filter, target) : ItemStack.isSameItemSameComponents(filter, target);
      } else {
         return false;
      }
   }

   private static boolean canStacksMerge(ItemStack a, ItemStack b) {
      return !a.isEmpty() && !b.isEmpty() ? ItemStack.isSameItemSameComponents(a, b) : false;
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   public static final class InventoryScan {
      public boolean hasSlots;
      public boolean foundItems;
      public boolean foundSpace;
   }
}
