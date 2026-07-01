package buildcraft.lib.fabric.transfer.fluid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class CombinedFluidStorage implements Storage<FluidVariant> {
   private final List<Storage<FluidVariant>> insertSlots;
   private final List<Storage<FluidVariant>> extractSlots;

   public CombinedFluidStorage(List<Storage<FluidVariant>> insertSlots, List<Storage<FluidVariant>> extractSlots) {
      this.insertSlots = List.copyOf(insertSlots);
      this.extractSlots = List.copyOf(extractSlots);
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long remaining = maxAmount;
         long totalInserted = 0L;

         for (Storage<FluidVariant> slot : this.insertSlots) {
            if (remaining <= 0L) {
               break;
            }

            long accepted = slot.insert(resource, remaining, transaction);
            if (accepted > 0L) {
               totalInserted += accepted;
               remaining -= accepted;
            }
         }

         return totalInserted;
      } else {
         return 0L;
      }
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long remaining = maxAmount;
         long totalExtracted = 0L;

         for (Storage<FluidVariant> slot : this.extractSlots) {
            if (remaining <= 0L) {
               break;
            }

            long extracted = slot.extract(resource, remaining, transaction);
            if (extracted > 0L) {
               totalExtracted += extracted;
               remaining -= extracted;
            }
         }

         return totalExtracted;
      } else {
         return 0L;
      }
   }

   public Iterator<StorageView<FluidVariant>> iterator() {
      Set<Storage<FluidVariant>> seen = new LinkedHashSet<>();
      seen.addAll(this.insertSlots);
      seen.addAll(this.extractSlots);
      List<StorageView<FluidVariant>> views = new ArrayList<>();

      for (Storage<FluidVariant> slot : seen) {
         for (StorageView<FluidVariant> view : slot) {
            views.add(view);
         }
      }

      return views.iterator();
   }
}
