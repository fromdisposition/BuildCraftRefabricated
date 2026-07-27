package buildcraft.lib.fabric.transfer;

import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import org.jspecify.annotations.Nullable;

public final class ItemStorageResolve {
   private ItemStorageResolve() {
   }

   public static @Nullable Storage<ItemVariant> combine(List<Storage<ItemVariant>> parts) {
      if (parts.isEmpty()) {
         return null;
      } else {
         return parts.size() == 1 ? parts.get(0) : new CombinedStorage<>(parts);
      }
   }
}
