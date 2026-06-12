/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.list;

import buildcraft.api.lists.ListMatchHandler;
import buildcraft.lib.fabric.transfer.ItemFluidLookup;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.List;
import javax.annotation.Nonnull;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ListMatchHandlerFluid extends ListMatchHandler {
   @Nullable
   private static Storage<FluidVariant> storageOf(@Nonnull ItemStack stack) {
      return stack.isEmpty() ? null : ItemFluidLookup.storage(stack);
   }

   @Nullable
   private static FluidStack firstFluid(@Nonnull ItemStack stack) {
      return ItemFluidLookup.firstFluid(storageOf(stack));
   }

   @Override
   public boolean isValidSource(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      switch (type) {
         case TYPE:
            return storageOf(stack) != null;
         case MATERIAL:
            FluidStack fluid = firstFluid(stack);
            return fluid != null && !fluid.isEmpty();
         default:
            return false;
      }
   }

   @Nonnull
   @Override
   public List<String> describeMatch(ListMatchHandler.Type type, @Nonnull ItemStack stack) {
      switch (type) {
         case TYPE:
            if (storageOf(stack) != null) {
               return List.of("any fluid container");
            }

            return List.of();
         case MATERIAL:
            FluidStack fluid = firstFluid(stack);
            if (fluid != null && !fluid.isEmpty()) {
               Identifier id = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
               return List.of("fluid: " + (id != null ? id.toString() : fluid.getFluid().toString()));
            }

            return List.of();
         default:
            return List.of();
      }
   }

   @Override
   public boolean matches(ListMatchHandler.Type type, @Nonnull ItemStack source, @Nonnull ItemStack target, boolean precise) {
      switch (type) {
         case TYPE:
            return storageOf(source) != null && storageOf(target) != null;
         case MATERIAL:
            FluidStack a = firstFluid(source);
            FluidStack b = firstFluid(target);
            if (a != null && b != null && !a.isEmpty() && !b.isEmpty()) {
               return FluidStack.isSameFluidSameComponents(a, b);
            }

            return false;
         default:
            return false;
      }
   }
}
