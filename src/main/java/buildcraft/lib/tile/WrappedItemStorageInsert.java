package buildcraft.lib.tile;

import buildcraft.lib.fabric.transfer.FabricItemStorageProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WrappedItemStorageInsert implements FabricItemStorageProvider {
   private final FabricItemStorageProvider delegate;
   private final Storage<ItemVariant> storage;

   public WrappedItemStorageInsert(FabricItemStorageProvider delegate) {
      this.delegate = delegate;
      final Storage<ItemVariant> inner = delegate.fabricItemStorage();
      this.storage = inner == null ? null : new InsertionOnlyStorage<ItemVariant>() {
         public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return inner.insert(resource, maxAmount, transaction);
         }
      };
   }

   public FabricItemStorageProvider delegate() {
      return this.delegate;
   }

   @Override
   public Storage<ItemVariant> fabricItemStorage() {
      return this.storage;
   }
}
