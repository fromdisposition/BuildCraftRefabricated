package buildcraft.lib.platform.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class TransferCommits {
   private TransferCommits() {
   }

   public static int saturateMb(long millibuckets) {
      return millibuckets > 2147483647L ? Integer.MAX_VALUE : (int)millibuckets;
   }

   public static int saturateCount(long count) {
      return count > 2147483647L ? Integer.MAX_VALUE : (int)count;
   }

   public static int insertItems(Storage<ItemVariant> storage, ItemStack stack, boolean commit) {
      return stack.isEmpty() ? 0 : insertItems(storage, stack, stack.getCount(), commit);
   }

   public static int insertItems(Storage<ItemVariant> storage, ItemStack template, int amount, boolean commit) {
      if (storage != null && !template.isEmpty() && amount > 0) {
         ItemVariant variant = ItemVariant.of(template);
         try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(variant, amount, transaction);
            int moved = saturateCount(inserted);
            if (commit && moved > 0) {
               transaction.commit();
            }

            return moved;
         }
      } else {
         return 0;
      }
   }
}
