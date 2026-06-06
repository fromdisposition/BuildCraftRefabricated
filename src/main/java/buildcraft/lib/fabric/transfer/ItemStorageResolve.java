package buildcraft.lib.fabric.transfer;

import buildcraft.lib.tile.CombinedItemStorageProvider;
import buildcraft.lib.tile.WrappedItemStorageExtract;
import buildcraft.lib.tile.WrappedItemStorageInsert;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class ItemStorageResolve {
   private ItemStorageResolve() {
   }

   public static @Nullable Storage<ItemVariant> of(@Nullable FabricItemStorageProvider provider) {
      if (provider == null) {
         return null;
      } else if (provider instanceof WrappedItemStorageExtract extract) {
         return extract.fabricItemStorage();
      } else if (provider instanceof WrappedItemStorageInsert insert) {
         return insert.fabricItemStorage();
      } else {
         return provider instanceof CombinedItemStorageProvider combined ? combined.fabricItemStorage() : provider.fabricItemStorage();
      }
   }

   public static @Nullable Storage<ItemVariant> combine(List<Storage<ItemVariant>> parts) {
      if (parts.isEmpty()) {
         return null;
      } else {
         return (Storage<ItemVariant>)(parts.size() == 1 ? parts.get(0) : new CombinedStorage(parts));
      }
   }

   public static @Nullable Storage<ItemVariant> combineProviders(FabricItemStorageProvider[] providers) {
      List<Storage<ItemVariant>> parts = new ArrayList<>(providers.length);

      for (FabricItemStorageProvider provider : providers) {
         Storage<ItemVariant> storage = of(provider);
         if (storage != null) {
            parts.add(storage);
         }
      }

      return combine(parts);
   }

   public static @Nullable Storage<ItemVariant> insertionOnly(final @Nullable Storage<ItemVariant> inner) {
      return inner == null ? null : new InsertionOnlyStorage<ItemVariant>() {
         public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return inner.insert(resource, maxAmount, transaction);
         }
      };
   }

   public static @Nullable Storage<ItemVariant> extractionOnly(final @Nullable Storage<ItemVariant> inner) {
      return inner == null ? null : new ExtractionOnlyStorage<ItemVariant>() {
         public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return inner.extract(resource, maxAmount, transaction);
         }

         public Iterator<StorageView<ItemVariant>> iterator() {
            return inner.iterator();
         }
      };
   }
}
