package buildcraft.lib.fabric.transfer;

import buildcraft.core.BCCoreItems;
import buildcraft.core.item.ItemFragileFluidContainer;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.transfer.fabric.TransferConvert;
import java.util.Collections;
import java.util.Iterator;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class FragileFluidContainerStorage implements Storage<FluidVariant> {
   private final ContainerItemContext context;

   private FragileFluidContainerStorage(ContainerItemContext context) {
      this.context = context;
   }

   public static @Nullable Storage<FluidVariant> of(ContainerItemContext context) {
      ItemStack stack = stack(context);
      return !stack.isEmpty() && BCCoreItems.FRAGILE_FLUID_CONTAINER != null && stack.getItem() == BCCoreItems.FRAGILE_FLUID_CONTAINER
         ? new FragileFluidContainerStorage(context)
         : null;
   }

   private static ItemStack stack(ContainerItemContext context) {
      return context.getItemVariant().isBlank() ? ItemStack.EMPTY : context.getItemVariant().toStack(saturate(context.getAmount()));
   }

   private static int saturate(long amount) {
      return amount > 2147483647L ? Integer.MAX_VALUE : (int)amount;
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         ItemStack stack = stack(this.context);
         if (!stack.isEmpty() && stack.getItem() == BCCoreItems.FRAGILE_FLUID_CONTAINER) {
            FluidStack current = ItemFragileFluidContainer.getFluid(stack);
            FluidStack incoming = TransferConvert.toFluidStack(resource);
            if (!current.isEmpty() && !FluidUtilBC.areEquivalentFluidStacks(current.copyWithAmount(1), incoming)) {
               return 0L;
            }

            int capacity = 500;
            int currentMb = current.getAmount();
            long maxMb = TransferConvert.dropletsToMb(maxAmount);
            int toInsertMb = (int)Math.min(maxMb, capacity - currentMb);
            if (toInsertMb <= 0) {
               return 0L;
            }

            FluidStack updated = current.isEmpty() ? incoming.copyWithAmount(toInsertMb) : current.copy();
            if (!current.isEmpty()) {
               updated.grow(toInsertMb);
            }

            ItemStack newStack = stack.copy();
            ItemFragileFluidContainer.setFluid(newStack, updated);
            long exchanged = this.context.exchange(ItemVariant.of(newStack), stack.getCount(), transaction);
            return exchanged > 0L ? TransferConvert.mbToDroplets(toInsertMb) : 0L;
         } else {
            return 0L;
         }
      } else {
         return 0L;
      }
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (maxAmount <= 0L) {
         return 0L;
      }

      ItemStack stack = stack(this.context);
      if (!stack.isEmpty() && stack.getItem() == BCCoreItems.FRAGILE_FLUID_CONTAINER) {
         FluidStack current = ItemFragileFluidContainer.getFluid(stack);
         if (current.isEmpty()) {
            return 0L;
         }

         if (!resource.isBlank() && !FluidUtilBC.areEquivalentFluidStacks(current.copyWithAmount(1), TransferConvert.toFluidStack(resource))) {
            return 0L;
         }

         long maxMb = TransferConvert.dropletsToMb(maxAmount);
         int toExtractMb = (int)Math.min(maxMb, current.getAmount());
         if (toExtractMb <= 0) {
            return 0L;
         }

         FluidStack updated = current.copy();
         updated.shrink(toExtractMb);
         ItemStack newStack = stack.copy();
         ItemFragileFluidContainer.setFluid(newStack, updated);
         long exchanged = this.context.exchange(ItemVariant.of(newStack), stack.getCount(), transaction);
         return exchanged > 0L ? TransferConvert.mbToDroplets(toExtractMb) : 0L;
      } else {
         return 0L;
      }
   }

   public Iterator<StorageView<FluidVariant>> iterator() {
      ItemStack stack = stack(this.context);
      FluidStack fluid = ItemFragileFluidContainer.getFluid(stack);
      if (fluid.isEmpty()) {
         return Collections.emptyIterator();
      }

      FluidVariant variant = TransferConvert.toVariant(fluid);
      long amount = TransferConvert.mbToDroplets(fluid.getAmount());
      return (Iterator<StorageView<FluidVariant>>)(Iterator<?>)Collections.singletonList(new FragileFluidContainerStorage.SingleView(variant, amount)).iterator();
   }

   private final class SingleView implements StorageView<FluidVariant> {
      private final FluidVariant resource;
      private final long amount;

      private SingleView(FluidVariant resource, long amount) {
         this.resource = resource;
         this.amount = amount;
      }

      public long extract(FluidVariant extractResource, long maxAmount, TransactionContext transaction) {
         return FragileFluidContainerStorage.this.extract(extractResource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return this.resource.isBlank();
      }

      public FluidVariant getResource() {
         return this.resource;
      }

      public long getAmount() {
         return this.amount;
      }

      public long getCapacity() {
         return TransferConvert.mbToDroplets(500L);
      }
   }
}
