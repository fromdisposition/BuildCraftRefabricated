package buildcraft.lib.tile;

import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import buildcraft.lib.fabric.transfer.ItemStorageResolve;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CombinedItemStorageProvider implements FabricItemStorageProvider {
   private final FabricItemStorageProvider[] providers;
   private final int[] baseIndex;
   private final int slotCount;
   private final Storage<ItemVariant> combined;

   @SafeVarargs
   public CombinedItemStorageProvider(FabricItemStorageProvider... providers) {
      this.providers = providers;
      this.baseIndex = new int[providers.length];
      int index = 0;

      for (int i = 0; i < providers.length; i++) {
         index += slotCount(providers[i]);
         this.baseIndex[i] = index;
      }

      this.slotCount = index;
      this.combined = ItemStorageResolve.combineProviders(providers);
   }

   @Override
   public @Nullable Storage<ItemVariant> fabricItemStorage() {
      return this.combined;
   }

   public ItemStack getFilter(int slot) {
      int providerIndex = this.getIndexForSlot(slot);
      int localSlot = this.getSlotFromIndex(slot, providerIndex);
      FabricItemStorageProvider provider = this.providers[providerIndex];
      if (provider instanceof ItemHandlerFiltered filtered) {
         return filtered.getFilter(localSlot);
      } else {
         return provider instanceof BcItemInventory inventory ? inventory.getStackInSlot(localSlot) : ItemStack.EMPTY;
      }
   }

   private int getIndexForSlot(int slot) {
      if (slot >= 0 && slot < this.slotCount) {
         for (int i = 0; i < this.baseIndex.length; i++) {
            if (slot < this.baseIndex[i]) {
               return i;
            }
         }

         return -1;
      } else {
         throw new IndexOutOfBoundsException("Slot " + slot + " not in valid range - [0," + this.slotCount + ")");
      }
   }

   private int getSlotFromIndex(int slot, int index) {
      if (index != -1 && index < this.baseIndex.length) {
         return index == 0 ? slot : slot - this.baseIndex[index - 1];
      } else {
         return -1;
      }
   }

   private static int slotCount(FabricItemStorageProvider provider) {
      if (provider instanceof BcItemInventory inventory) {
         return inventory.getSlots();
      } else {
         Storage<ItemVariant> storage = provider.fabricItemStorage();
         if (storage == null) {
            return 0;
         }

         int count = 0;

         for (StorageView<ItemVariant> ignored : storage) {
            count++;
         }

         return count;
      }
   }
}
