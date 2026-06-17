package buildcraft.lib.platform.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class EnergyStorageOps {
   private EnergyStorageOps() {
   }

   public static int extract(@Nullable EnergyStorage storage, int maxAmount, boolean commit) {
      if (storage != null && maxAmount > 0 && storage.supportsExtraction()) {
         try (Transaction transaction = Transaction.openOuter()) {
            int extracted = extract(storage, maxAmount, transaction);
            if (commit && extracted > 0) {
               transaction.commit();
            }

            return extracted;
         }
      } else {
         return 0;
      }
   }

   public static int extract(EnergyStorage storage, int maxAmount, TransactionContext transaction) {
      return storage != null && maxAmount > 0 && storage.supportsExtraction() ? saturate(storage.extract(maxAmount, transaction)) : 0;
   }

   public static int insert(@Nullable EnergyStorage storage, int amount, boolean commit) {
      if (storage != null && amount > 0 && storage.supportsInsertion()) {
         try (Transaction transaction = Transaction.openOuter()) {
            int inserted = insert(storage, amount, transaction);
            if (commit && inserted > 0) {
               transaction.commit();
            }

            return inserted;
         }
      } else {
         return 0;
      }
   }

   public static int insert(EnergyStorage storage, int amount, TransactionContext transaction) {
      return storage != null && amount > 0 && storage.supportsInsertion() ? saturate(storage.insert(amount, transaction)) : 0;
   }

   public static long spareCapacity(@Nullable EnergyStorage storage) {
      return storage == null ? 0L : Math.max(0L, storage.getCapacity() - storage.getAmount());
   }

   public static boolean canAccept(@Nullable EnergyStorage storage) {
      if (storage != null && storage.supportsInsertion()) {
         try (Transaction transaction = Transaction.openOuter()) {
            return storage.insert(1L, transaction) > 0L;
         }
      } else {
         return false;
      }
   }

   public static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }
}
