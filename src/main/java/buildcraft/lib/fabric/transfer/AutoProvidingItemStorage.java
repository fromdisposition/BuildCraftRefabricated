package buildcraft.lib.fabric.transfer;

import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public enum AutoProvidingItemStorage implements Storage<ItemVariant> {
   INSTANCE;

   @Override
   public boolean supportsInsertion() {
      return false;
   }

   @Override
   public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      return 0L;
   }

   @Override
   public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
      return 0L;
   }

   @Override
   public Iterator<StorageView<ItemVariant>> iterator() {
      return Collections.emptyIterator();
   }
}
