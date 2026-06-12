package buildcraft.lib.fabric.transfer;

import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.fabric.transfer.FluidVariants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class MultiFluidTankStorage implements Storage<FluidVariant> {
   private final SingleFluidTank[] tanks;

   public MultiFluidTankStorage(SingleFluidTank[] tanks) {
      this.tanks = tanks;
   }

   public int size() {
      return this.tanks.length;
   }

   public SingleFluidTank tank(int index) {
      return this.tanks[index];
   }

   public FluidStack getFluidStack(int index) {
      return index >= 0 && index < this.tanks.length ? this.tanks[index].getFluidStack() : FluidStack.EMPTY;
   }

   public int getAmountMb(int index) {
      return index >= 0 && index < this.tanks.length ? this.tanks[index].getAmountMb() : 0;
   }

   public int insertMillibuckets(FluidStack fluid, int maxMb, boolean commit) {
      if (!fluid.isEmpty() && maxMb > 0) {
         try (Transaction transaction = Transaction.openOuter()) {
            FluidVariant variant = FluidVariants.toVariant(fluid);
            long remaining = FluidVariants.mbToDroplets(maxMb);
            int insertedMb = 0;

            for (SingleFluidTank tank : this.tanks) {
               if (remaining <= 0L) {
                  break;
               }

               long moved = tank.insert(variant, remaining, transaction);
               int movedMb = (int)Math.min(FluidVariants.dropletsToMb(moved), 2147483647L);
               insertedMb += movedMb;
               remaining -= moved;
            }

            if (commit) {
               transaction.commit();
            }

            return insertedMb;
         }
      } else {
         return 0;
      }
   }

   public int extractMillibuckets(FluidStack fluid, int maxMb, boolean commit) {
      if (!fluid.isEmpty() && maxMb > 0) {
         try (Transaction transaction = Transaction.openOuter()) {
            FluidVariant variant = FluidVariants.toVariant(fluid);
            long remaining = FluidVariants.mbToDroplets(maxMb);
            int extractedMb = 0;

            for (SingleFluidTank tank : this.tanks) {
               if (remaining <= 0L) {
                  break;
               }

               long moved = tank.extract(variant, remaining, transaction);
               int movedMb = (int)Math.min(FluidVariants.dropletsToMb(moved), 2147483647L);
               extractedMb += movedMb;
               remaining -= moved;
            }

            if (commit) {
               transaction.commit();
            }

            return extractedMb;
         }
      } else {
         return 0;
      }
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long remaining = maxAmount;
         long inserted = 0L;

         for (SingleFluidTank tank : this.tanks) {
            if (remaining <= 0L) {
               break;
            }

            long moved = tank.insert(resource, remaining, transaction);
            inserted += moved;
            remaining -= moved;
         }

         return inserted;
      } else {
         return 0L;
      }
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long remaining = maxAmount;
         long extracted = 0L;

         for (SingleFluidTank tank : this.tanks) {
            if (remaining <= 0L) {
               break;
            }

            long moved = tank.extract(resource, remaining, transaction);
            extracted += moved;
            remaining -= moved;
         }

         return extracted;
      } else {
         return 0L;
      }
   }

   public Iterator<StorageView<FluidVariant>> iterator() {
      List<StorageView<FluidVariant>> views = new ArrayList<>();

      for (SingleFluidTank tank : this.tanks) {
         for (StorageView<FluidVariant> view : tank) {
            if (!view.isResourceBlank()) {
               views.add(view);
            }
         }
      }

      return views.iterator();
   }
}
