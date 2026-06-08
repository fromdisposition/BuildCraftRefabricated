package buildcraft.lib.fabric.transfer;

import buildcraft.lib.common.util.ValueIOSerializable;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.FluidUtilBC;
import buildcraft.lib.fabric.transfer.TransferCommits;
import buildcraft.lib.transfer.fabric.TransferConvert;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SingleFluidTank implements Storage<FluidVariant>, ValueIOSerializable {
   public static final String VALUE_IO_KEY = "fluidStack";
   private static final String LEGACY_VALUE_IO_KEY = "stacks";
   private static final Codec<NonNullList<FluidStack>> STACKS_CODEC = FluidStack.OPTIONAL_CODEC.listOf().xmap(SingleFluidTank::copyStacks, Function.identity());
   private FluidStack contents = FluidStack.EMPTY;
   private final int capacityMb;
   private final SingleFluidTank.TankAccess access;
   private final Runnable onContentsChanged;
   private final ArrayList<FluidStack> transactionSnapshots = new ArrayList<>();
   private final BitSet transactionHooked = new BitSet();

   public SingleFluidTank(int capacityMb) {
      this(capacityMb, SingleFluidTank.TankAccess.OPEN, null);
   }

   public SingleFluidTank(int capacityMb, SingleFluidTank.TankAccess access) {
      this(capacityMb, access, null);
   }

   public SingleFluidTank(int capacityMb, SingleFluidTank.TankAccess access, Runnable onContentsChanged) {
      this.capacityMb = capacityMb;
      this.access = access;
      this.onContentsChanged = onContentsChanged;
   }

   public int getCapacityMb() {
      return this.capacityMb;
   }

   public int getAmountMb() {
      return this.contents.isEmpty() ? 0 : this.contents.getAmount();
   }

   public FluidStack getFluidStack() {
      return this.contents.isEmpty() ? FluidStack.EMPTY : this.contents.copy();
   }

   public boolean isEmpty() {
      return this.contents.isEmpty() || this.contents.getAmount() <= 0;
   }

   public void setContents(FluidStack stack) {
      if (stack != null && !stack.isEmpty()) {
         this.contents = stack.copyWithAmount(Math.min(stack.getAmount(), this.capacityMb));
      } else {
         this.contents = FluidStack.EMPTY;
      }

      this.notifyContentsChanged();
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      return !this.access.externalInsert() ? 0L : this.insertUnchecked(resource, maxAmount, transaction, true);
   }

   public long insertInternal(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      return this.insertUnchecked(resource, maxAmount, transaction, false);
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      return !this.access.externalExtract() ? 0L : this.extractUnchecked(resource, maxAmount, transaction);
   }

   public long extractInternal(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      return this.extractUnchecked(resource, maxAmount, transaction);
   }

   @SuppressWarnings("unchecked")
   public Iterator<StorageView<FluidVariant>> iterator() {
      return (Iterator<StorageView<FluidVariant>>)(Iterator<?>)List.of(new SingleFluidTank.SlotView()).iterator();
   }

   @Override
   public void serialize(ValueOutput output) {
      NonNullList<FluidStack> stacks = NonNullList.withSize(1, FluidStack.EMPTY);
      stacks.set(0, this.isEmpty() ? FluidStack.EMPTY : this.contents.copy());
      output.store(VALUE_IO_KEY, STACKS_CODEC, stacks);
   }

   @Override
   public void deserialize(ValueInput input) {
      input.read(VALUE_IO_KEY, STACKS_CODEC).or(() -> input.read(LEGACY_VALUE_IO_KEY, STACKS_CODEC)).ifPresent(stacks -> {
         if (stacks.isEmpty()) {
            this.contents = FluidStack.EMPTY;
         } else {
            FluidStack loaded = (FluidStack)stacks.getFirst();
            if (!loaded.isEmpty() && loaded.getAmount() > 0) {
               int amount = Math.min(loaded.getAmount(), this.capacityMb);
               this.contents = loaded.copyWithAmount(amount);
            } else {
               this.contents = FluidStack.EMPTY;
            }
         }
      });
   }

   private long insertUnchecked(FluidVariant resource, long maxAmount, TransactionContext transaction, boolean applyFilter) {
      if (!resource.isBlank() && maxAmount > 0L) {
         FluidStack fluid = FluidUtilBC.canonicalFluidStack(TransferConvert.toFluidStack(resource));
         if (fluid.isEmpty()) {
            return 0L;
         }

         if (applyFilter && !this.access.insertFilter().test(fluid)) {
            return 0L;
         }

         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         int insertMb = TransferCommits.saturateMb(millibuckets);
         if (!this.isEmpty() && !FluidUtilBC.areEquivalentFluidStacks(this.getFluidStack(), fluid)) {
            return 0L;
         }

         int space = this.capacityMb - this.getAmountMb();
         int acceptedMb = Math.min(insertMb, space);
         if (acceptedMb <= 0) {
            return 0L;
         }

         this.updateSnapshots(transaction);
         if (this.isEmpty()) {
            this.contents = fluid.copyWithAmount(acceptedMb);
         } else {
            this.contents = this.getFluidStack().copyWithAmount(this.getAmountMb() + acceptedMb);
         }

         this.notifyContentsChanged();
         return TransferConvert.mbToDroplets(acceptedMb);
      } else {
         return 0L;
      }
   }

   private long extractUnchecked(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L && !this.isEmpty()) {
         FluidStack fluid = FluidUtilBC.canonicalFluidStack(TransferConvert.toFluidStack(resource));
         if (!FluidUtilBC.areEquivalentFluidStacks(this.getFluidStack(), fluid)) {
            return 0L;
         }

         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         int extractMb = Math.min(TransferCommits.saturateMb(millibuckets), this.getAmountMb());
         if (extractMb <= 0) {
            return 0L;
         }

         this.updateSnapshots(transaction);
         int remaining = this.getAmountMb() - extractMb;
         this.contents = remaining <= 0 ? FluidStack.EMPTY : this.getFluidStack().copyWithAmount(remaining);
         this.notifyContentsChanged();
         return TransferConvert.mbToDroplets(extractMb);
      } else {
         return 0L;
      }
   }

   private void notifyContentsChanged() {
      if (this.onContentsChanged != null) {
         this.onContentsChanged.run();
      }
   }

   private void updateSnapshots(TransactionContext transaction) {
      int depth = transaction.nestingDepth();

      while (this.transactionSnapshots.size() <= depth) {
         this.transactionSnapshots.add(null);
      }

      if (this.transactionSnapshots.get(depth) == null) {
         this.transactionSnapshots.set(depth, this.copyContents());
         if (!this.transactionHooked.get(depth)) {
            int hookedDepth = depth;
            transaction.getOpenTransaction(depth).addCloseCallback((context, result) -> this.onTransactionClose(hookedDepth, result.wasCommitted()));
            this.transactionHooked.set(depth);
         }
      }
   }

   private void onTransactionClose(int depth, boolean committed) {
      this.transactionHooked.clear(depth);
      if (!committed) {
         this.contents = this.transactionSnapshots.get(depth);
      }

      this.transactionSnapshots.set(depth, null);
   }

   private FluidStack copyContents() {
      return this.isEmpty() ? FluidStack.EMPTY : this.contents.copy();
   }

   private static NonNullList<FluidStack> copyStacks(List<FluidStack> stacks) {
      NonNullList<FluidStack> copy = NonNullList.withSize(Math.max(1, stacks.size()), FluidStack.EMPTY);

      for (int i = 0; i < stacks.size(); i++) {
         copy.set(i, stacks.get(i));
      }

      return copy;
   }

   private final class SlotView implements StorageView<FluidVariant> {
      public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
         return SingleFluidTank.this.extract(resource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return SingleFluidTank.this.isEmpty();
      }

      public FluidVariant getResource() {
         return SingleFluidTank.this.isEmpty() ? FluidVariant.blank() : TransferConvert.toVariant(SingleFluidTank.this.getFluidStack());
      }

      public long getAmount() {
         return TransferConvert.mbToDroplets(SingleFluidTank.this.getAmountMb());
      }

      public long getCapacity() {
         return TransferConvert.mbToDroplets(SingleFluidTank.this.capacityMb);
      }
   }

   public record TankAccess(Predicate<FluidStack> insertFilter, boolean externalInsert, boolean externalExtract) {
      public static final SingleFluidTank.TankAccess OPEN = new SingleFluidTank.TankAccess(r -> true, true, true);
      public static final SingleFluidTank.TankAccess MACHINE_OUTPUT = new SingleFluidTank.TankAccess(r -> false, false, true);

      public static SingleFluidTank.TankAccess filteredInput(Predicate<FluidStack> filter) {
         return new SingleFluidTank.TankAccess(filter, true, false);
      }
   }
}
