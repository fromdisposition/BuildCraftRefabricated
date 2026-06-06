package buildcraft.transport.pipe.flow;

import buildcraft.api.core.IStackFilter;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fabric.TransferConvert;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class PipeNeighborTransfers {
   private PipeNeighborTransfers() {
   }

   public static @Nullable FluidStack firstFluid(Storage<FluidVariant> storage) {
      if (storage == null) {
         return null;
      }

      for (StorageView<FluidVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            return TransferConvert.toFluidStack((FluidVariant)view.getResource());
         }
      }

      return null;
   }

   public static int extractFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int maxMillibuckets, boolean commit) {
      if (storage != null && fluid != null && !fluid.isEmpty() && maxMillibuckets > 0) {
         FluidVariant variant = TransferConvert.toVariant(fluid);
         Transaction tx = Transaction.openOuter();

         int var7;
         try {
            int extracted = extractFluidMb(storage, variant, maxMillibuckets, tx);
            if (commit && extracted > 0) {
               tx.commit();
            }

            var7 = extracted;
         } catch (Throwable var9) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (tx != null) {
            tx.close();
         }

         return var7;
      } else {
         return 0;
      }
   }

   public static int extractFluidMb(Storage<FluidVariant> storage, FluidVariant variant, int maxMillibuckets, TransactionContext transaction) {
      if (storage != null && !variant.isBlank() && maxMillibuckets > 0) {
         long extracted = storage.extract(variant, TransferConvert.mbToDroplets(maxMillibuckets), transaction);
         return saturateMb(TransferConvert.dropletsToMb(extracted));
      } else {
         return 0;
      }
   }

   public static int insertFluidMb(Storage<FluidVariant> storage, FluidStack fluid, int millibuckets, boolean commit) {
      if (storage != null && fluid != null && !fluid.isEmpty() && millibuckets > 0) {
         FluidVariant variant = TransferConvert.toVariant(fluid);
         Transaction tx = Transaction.openOuter();

         int var7;
         try {
            int inserted = insertFluidMb(storage, variant, millibuckets, tx);
            if (commit && inserted > 0) {
               tx.commit();
            }

            var7 = inserted;
         } catch (Throwable var9) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (tx != null) {
            tx.close();
         }

         return var7;
      } else {
         return 0;
      }
   }

   public static int insertFluidMb(Storage<FluidVariant> storage, FluidVariant variant, int millibuckets, TransactionContext transaction) {
      if (storage != null && !variant.isBlank() && millibuckets > 0) {
         long inserted = storage.insert(variant, TransferConvert.mbToDroplets(millibuckets), transaction);
         return saturateMb(TransferConvert.dropletsToMb(inserted));
      } else {
         return 0;
      }
   }

   public static PipeNeighborTransfers.@Nullable ItemProbe findMatchingItem(Storage<ItemVariant> storage, int maxCount, IStackFilter filter) {
      if (storage == null) {
         return null;
      }

      for (StorageView<ItemVariant> view : storage) {
         if (!view.isResourceBlank() && view.getAmount() > 0L) {
            int available = saturate(view.getAmount());
            ItemStack stack = ((ItemVariant)view.getResource()).toStack(Math.min(available, maxCount));
            if (filter.matches(stack)) {
               return new PipeNeighborTransfers.ItemProbe(view, (ItemVariant)view.getResource(), available);
            }
         }
      }

      return null;
   }

   public static int extractFromView(StorageView<ItemVariant> view, ItemVariant variant, int amount, boolean commit) {
      if (view != null && !variant.isBlank() && amount > 0) {
         Transaction tx = Transaction.openOuter();

         int var8;
         try {
            long extracted = view.extract(variant, amount, tx);
            int moved = saturate(extracted);
            if (commit && moved > 0) {
               tx.commit();
            }

            var8 = moved;
         } catch (Throwable var10) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (tx != null) {
            tx.close();
         }

         return var8;
      } else {
         return 0;
      }
   }

   public static int insertItems(Storage<ItemVariant> storage, ItemStack stack, boolean commit) {
      if (storage != null && !stack.isEmpty()) {
         ItemVariant variant = ItemVariant.of(stack);
         Transaction tx = Transaction.openOuter();

         int var8;
         try {
            long inserted = storage.insert(variant, stack.getCount(), tx);
            int moved = saturate(inserted);
            if (commit && moved > 0) {
               tx.commit();
            }

            var8 = moved;
         } catch (Throwable var10) {
            if (tx != null) {
               try {
                  tx.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }
            }

            throw var10;
         }

         if (tx != null) {
            tx.close();
         }

         return var8;
      } else {
         return 0;
      }
   }

   private static int saturateMb(long millibuckets) {
      return millibuckets > 2147483647L ? Integer.MAX_VALUE : (int)millibuckets;
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   public record ItemProbe(StorageView<ItemVariant> view, ItemVariant resource, int available) {
   }
}
