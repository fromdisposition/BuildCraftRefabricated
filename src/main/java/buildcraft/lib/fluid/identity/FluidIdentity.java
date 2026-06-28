/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.identity;

import buildcraft.lib.fluid.stack.FluidStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidIdentity {
   private FluidIdentity() {
   }

   public static List<FluidStack> mergeSameFluids(List<FluidStack> fluids) {
      List<FluidStack> stacks = new ArrayList<>();
      fluids.forEach(toAdd -> {
         boolean found = false;

         for (FluidStack stack : stacks) {
            if (FluidStack.isSameFluidSameComponents(stack, toAdd)) {
               stack.grow(toAdd.getAmount());
               found = true;
            }
         }

         if (!found) {
            stacks.add(toAdd.copy());
         }
      });
      return stacks;
   }

   public static boolean areFluidsEqual(Fluid a, Fluid b) {
      if (a == null || b == null) {
         return a == b;
      }

      if (a == b) {
         return true;
      }

      Identifier idA = BuiltInRegistries.FLUID.getKey(a);
      Identifier idB = BuiltInRegistries.FLUID.getKey(b);
      return idA.getNamespace().equals(idB.getNamespace()) && normalizeFluidPath(idA.getPath()).equals(normalizeFluidPath(idB.getPath()));
   }

   public static Fluid canonicalFluid(Fluid fluid) {
      if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
         Identifier fluidId = BuiltInRegistries.FLUID.getKey(fluid);
         if (fluidId == null) {
            return fluid;
         } else {
            String path = fluidId.getPath();
            if (path.startsWith("flowing_")) {
               Identifier stillId = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path.substring("flowing_".length()));
               //? if >= 1.21.10 {
               Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> (Fluid)ref.value()).orElse(Fluids.EMPTY);
               //?} else {
               /*Fluid still = BuiltInRegistries.FLUID.get(stillId);
               *///?}
               return still.isSame(Fluids.EMPTY) ? fluid : still;
            } else if (path.endsWith("_flowing")) {
               Identifier stillId = Identifier.fromNamespaceAndPath(fluidId.getNamespace(), path.substring(0, path.length() - "_flowing".length()));
               //? if >= 1.21.10 {
               Fluid still = BuiltInRegistries.FLUID.get(stillId).map(ref -> (Fluid)ref.value()).orElse(Fluids.EMPTY);
               //?} else {
               /*Fluid still = BuiltInRegistries.FLUID.get(stillId);
               *///?}
               return still.isSame(Fluids.EMPTY) ? fluid : still;
            } else {
               return fluid;
            }
         }
      } else {
         return Fluids.EMPTY;
      }
   }

   public static boolean areEquivalentFluidStacks(FluidStack a, FluidStack b) {
      if (!a.isEmpty() && !b.isEmpty()) {
         FluidStack ca = canonicalFluidStack(a);
         FluidStack cb = canonicalFluidStack(b);
         return FluidStack.isSameFluidSameComponents(ca, cb);
      } else {
         return a.isEmpty() && b.isEmpty();
      }
   }

   public static FluidStack canonicalFluidStack(FluidStack stack) {
      if (stack.isEmpty()) {
         return stack;
      }

      Fluid canonical = canonicalFluid(stack.getFluid());
      return canonical.isSame(stack.getFluid()) ? stack : new FluidStack(canonical, stack.getAmount(), stack.getComponentsPatch());
   }

   public static String normalizeFluidPath(String path) {
      if (path.startsWith("flowing_")) {
         return path.substring("flowing_".length());
      } else {
         return path.endsWith("_flowing") ? path.substring(0, path.length() - "_flowing".length()) : path;
      }
   }
}
