/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid;

import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.ICoolantManager;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.lib.fluids.FluidStack;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public enum CoolantRegistry implements ICoolantManager {
   INSTANCE;

   private final List<ICoolant> coolants = new LinkedList<>();
   private final List<ISolidCoolant> solidCoolants = new LinkedList<>();

   @Override
   public ICoolant addCoolant(ICoolant coolant) {
      this.coolants.add(coolant);
      return coolant;
   }

   @Override
   public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant) {
      this.solidCoolants.add(solidCoolant);
      return solidCoolant;
   }

   @Override
   public ICoolant addCoolant(FluidStack fluid, float degreesCoolingPerMb) {
      return this.addCoolant(new CoolantRegistry.Coolant(fluid, degreesCoolingPerMb));
   }

   @Override
   public ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
      return this.addSolidCoolant(new CoolantRegistry.SolidCoolant(solid, fluid, multiplier));
   }

   @Override
   public Collection<ICoolant> getCoolants() {
      return this.coolants;
   }

   @Override
   public Collection<ISolidCoolant> getSolidCoolants() {
      return this.solidCoolants;
   }

   @Override
   public ICoolant getCoolant(FluidStack fluid) {
      if (fluid != null && !fluid.isEmpty()) {
         for (ICoolant coolant : this.coolants) {
            if (coolant.matchesFluid(fluid)) {
               return coolant;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public float getDegreesPerMb(FluidStack fluid, float heat) {
      if (fluid != null && !fluid.isEmpty()) {
         for (ICoolant coolant : this.coolants) {
            float degrees = coolant.getDegreesCoolingPerMB(fluid, heat);
            if (degrees > 0.0F) {
               return degrees;
            }
         }

         return 0.0F;
      } else {
         return 0.0F;
      }
   }

   @Override
   public ISolidCoolant getSolidCoolant(ItemStack solid) {
      for (ISolidCoolant coolant : this.solidCoolants) {
         if (coolant.getFluidFromSolidCoolant(solid) != null) {
            return coolant;
         }
      }

      return null;
   }

   public static class Coolant implements ICoolant {
      private final FluidStack fluid;
      private final float degreesCoolingPerMb;

      public Coolant(FluidStack fluid, float degreesCoolingPerMb) {
         this.fluid = fluid;
         this.degreesCoolingPerMb = degreesCoolingPerMb;
      }

      @Override
      public boolean matchesFluid(FluidStack stack) {
         return FluidStack.isSameFluid(this.fluid, stack);
      }

      @Override
      public float getDegreesCoolingPerMB(FluidStack stack, float heat) {
         return this.matchesFluid(stack) ? this.degreesCoolingPerMb : 0.0F;
      }

      @Override
      public FluidStack getRepresentativeFluid() {
         return this.fluid;
      }
   }

   public static class SolidCoolant implements ISolidCoolant {
      private final ItemStack solid;
      private final FluidStack fluid;
      private final float multiplier;

      public SolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
         this.solid = solid;
         this.fluid = fluid;
         this.multiplier = multiplier;
      }

      @Override
      public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
         if (stack != null && ItemStack.isSameItem(stack, this.solid)) {
            int liquidAmount = (int)(stack.getCount() * this.fluid.getAmount() * this.multiplier / this.solid.getCount());
            return new FluidStack(this.fluid.getFluid(), liquidAmount);
         } else {
            return null;
         }
      }

      @Override
      public ItemStack getRepresentativeStack() {
         return this.solid;
      }
   }
}
