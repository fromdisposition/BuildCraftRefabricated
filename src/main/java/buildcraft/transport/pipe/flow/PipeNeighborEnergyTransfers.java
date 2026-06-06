package buildcraft.transport.pipe.flow;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public final class PipeNeighborEnergyTransfers {
   private PipeNeighborEnergyTransfers() {
   }

   public static int extract(@Nullable EnergyStorage storage, int maxAmount, boolean commit) {
      if (storage != null && maxAmount > 0 && storage.supportsExtraction()) {
         Transaction tx = Transaction.openOuter();

         int var5;
         try {
            int extracted = extract(storage, maxAmount, tx);
            if (commit && extracted > 0) {
               tx.commit();
            }

            var5 = extracted;
         } catch (Throwable var7) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (tx != null) {
            tx.close();
         }

         return var5;
      } else {
         return 0;
      }
   }

   public static int extract(EnergyStorage storage, int maxAmount, TransactionContext transaction) {
      return storage != null && maxAmount > 0 && storage.supportsExtraction() ? saturate(storage.extract(maxAmount, transaction)) : 0;
   }

   public static int insert(@Nullable EnergyStorage storage, int amount, boolean commit) {
      if (storage != null && amount > 0 && storage.supportsInsertion()) {
         Transaction tx = Transaction.openOuter();

         int var5;
         try {
            int inserted = insert(storage, amount, tx);
            if (commit && inserted > 0) {
               tx.commit();
            }

            var5 = inserted;
         } catch (Throwable var7) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (tx != null) {
            tx.close();
         }

         return var5;
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
         Transaction tx = Transaction.openOuter();

         boolean var2;
         try {
            var2 = storage.insert(1L, tx) > 0L;
         } catch (Throwable var5) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (tx != null) {
            tx.close();
         }

         return var2;
      } else {
         return false;
      }
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }
}
