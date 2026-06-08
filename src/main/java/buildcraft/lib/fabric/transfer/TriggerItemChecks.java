package buildcraft.lib.fabric.transfer;

import buildcraft.api.items.IList;
import buildcraft.lib.misc.StackUtil;
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
         for (StorageView<ItemVariant> ignored : storage) {
            return true;
         }

         return false;
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
         ItemStack stack = view.isResourceBlank() ? ItemStack.EMPTY : ((ItemVariant)view.getResource()).toStack(EnergyStorageOps.saturate(view.getAmount()));
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
         ItemStack stackInSlot = view.isResourceBlank() ? ItemStack.EMPTY : ((ItemVariant)view.getResource()).toStack(EnergyStorageOps.saturate(view.getAmount()));
         int slotCapacity = EnergyStorageOps.saturate(view.getCapacity());
         if (stackInSlot.isEmpty()) {
            if (searchStack.isEmpty()) {
               itemSpace += slotCapacity;
            } else if (!(searchStack.getItem() instanceof IList)) {
               int count = Math.min(slotCapacity, searchStack.getMaxStackSize());
               itemSpace += count;
            }
         } else if (searchStack.isEmpty() || StackUtil.isMatchingItemOrList(searchStack, stackInSlot)) {
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
      } else if (StackUtil.canMerge(stack, searchedStack) && stack.getCount() < stack.getMaxStackSize()) {
         int amount = Math.min(searchedStack.getCount(), stack.getMaxStackSize() - stack.getCount());
         return amount > 0 && EnergyStorageOps.saturate(view.getCapacity()) >= stack.getCount() + amount;
      } else {
         return false;
      }
   }

   private static boolean matchesSearch(ItemStack searchedStack, ItemStack stack, boolean isList, @Nullable IList listFilter) {
      if (searchedStack.isEmpty()) {
         return true;
      } else {
         return !isList ? StackUtil.canMerge(stack, searchedStack) : !stack.isEmpty() && listFilter != null && listFilter.matches(searchedStack, stack);
      }
   }

   public static final class InventoryScan {
      public boolean hasSlots;
      public boolean foundItems;
      public boolean foundSpace;
   }
}
