/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.transfer.fabric.TransferConvert;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class PipeFluidSectionStorage implements Storage<FluidVariant> {
   private final PipeFlowFluids.Section section;

   public PipeFluidSectionStorage(PipeFlowFluids.Section section) {
      this.section = section;
   }

   public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         int inserted = this.section.insert(0, TransferConvert.toFluidStack(resource), saturateMb(millibuckets), transaction);
         return TransferConvert.mbToDroplets(inserted);
      } else {
         return 0L;
      }
   }

   public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
      if (!resource.isBlank() && maxAmount > 0L) {
         long millibuckets = TransferConvert.dropletsToMb(maxAmount);
         if (millibuckets <= 0L) {
            return 0L;
         }

         int extracted = this.section.extract(0, TransferConvert.toFluidStack(resource), saturateMb(millibuckets), transaction);
         return TransferConvert.mbToDroplets(extracted);
      } else {
         return 0L;
      }
   }

   @SuppressWarnings("unchecked")
   public Iterator<StorageView<FluidVariant>> iterator() {
      FluidStack fluid = this.section.getFluidStack(0);
      return !fluid.isEmpty() && this.section.getAmountAsLong(0) > 0L
         ? (Iterator<StorageView<FluidVariant>>)(Iterator<?>)List.of(new PipeFluidSectionStorage.SectionView()).iterator()
         : Collections.emptyIterator();
   }

   private static int saturateMb(long millibuckets) {
      return millibuckets > 2147483647L ? Integer.MAX_VALUE : (int)millibuckets;
   }

   private final class SectionView implements StorageView<FluidVariant> {
      public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
         return PipeFluidSectionStorage.this.extract(resource, maxAmount, transaction);
      }

      public boolean isResourceBlank() {
         return PipeFluidSectionStorage.this.section.getFluidStack(0).isEmpty();
      }

      public FluidVariant getResource() {
         FluidStack fluid = PipeFluidSectionStorage.this.section.getFluidStack(0);
         return fluid.isEmpty() ? FluidVariant.blank() : TransferConvert.toVariant(fluid);
      }

      public long getAmount() {
         return TransferConvert.mbToDroplets(PipeFluidSectionStorage.this.section.getAmountAsLong(0));
      }

      public long getCapacity() {
         return TransferConvert.mbToDroplets(PipeFluidSectionStorage.this.section.getCapacityAsLong(0, PipeFluidSectionStorage.this.section.getFluidStack(0)));
      }
   }
}
