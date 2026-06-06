package buildcraft.lib.tile;

import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import buildcraft.lib.misc.INBTSerializable;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.CompoundTag;

public class ItemHandlerSimple extends BcItemInventory implements FabricItemStorageProvider, INBTSerializable<CompoundTag> {
   public ItemHandlerSimple(int size) {
      super(size);
   }

   public ItemHandlerSimple(int size, int maxStackSize) {
      super(size, maxStackSize);
   }

   public ItemHandlerSimple(int size, @Nullable StackChangeCallback callback) {
      super(size, callback);
   }

   public ItemHandlerSimple(int size, StackInsertionChecker checker, StackInsertionFunction insertionFunction, @Nullable StackChangeCallback callback) {
      super(size, checker, insertionFunction, callback);
   }

   @Override
   public Storage<ItemVariant> fabricItemStorage() {
      return this;
   }
}
