/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.tile;


import buildcraft.lib.fluid.identity.FluidIdentity;
import buildcraft.lib.fabric.transfer.fluid.SingleFluidTank;
import buildcraft.lib.fabric.transfer.fluid.ItemFluidLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.tile.craft.IFluidCraftSupport;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public final class FluidWorkbenchCraftSupport implements IFluidCraftSupport {
   private final List<SingleFluidTank> tanks;

   public FluidWorkbenchCraftSupport(SingleFluidTank tank1, SingleFluidTank tank2) {
      this.tanks = List.of(tank1, tank2);
   }

   @Override
   public boolean canSupply(ItemStack required, int count, boolean simulate) {
      FluidStack needed = ItemFluidLookup.firstFluid(required);
      if (needed.isEmpty() || count <= 0) {
         return false;
      }

      int mbNeeded = needed.getAmount() * count;
      return availableMb(needed) >= mbNeeded;
   }

   @Override
   public ItemStack extractForCraft(ItemStack required) {
      FluidStack needed = ItemFluidLookup.firstFluid(required);
      if (needed.isEmpty()) {
         return ItemStack.EMPTY;
      }

      int mbNeeded = needed.getAmount();
      Transaction tx = Transaction.openOuter();

      try {
         for (SingleFluidTank tank : this.tanks) {
            FluidStack held = tank.getFluidStack();
            if (held.isEmpty() || !FluidIdentity.areEquivalentFluidStacks(held.copyWithAmount(1), needed.copyWithAmount(1))) {
               continue;
            }

            if (tank.getAmountMb() < mbNeeded) {
               continue;
            }

            if (tank.extractMb(needed, mbNeeded, tx) >= mbNeeded) {
               tx.commit();
               return required.copyWithCount(1);
            }
         }
      } catch (Throwable e) {
         throw e;
      } finally {
         tx.close();
      }

      return ItemStack.EMPTY;
   }

   private int availableMb(FluidStack needed) {
      int total = 0;

      for (SingleFluidTank tank : this.tanks) {
         FluidStack held = tank.getFluidStack();
         if (!held.isEmpty() && FluidIdentity.areEquivalentFluidStacks(held.copyWithAmount(1), needed.copyWithAmount(1))) {
            total += tank.getAmountMb();
         }
      }

      return total;
   }
}
