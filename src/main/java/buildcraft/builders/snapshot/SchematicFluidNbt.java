/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.lib.fabric.BcRegistryUtil;
import buildcraft.lib.fluid.stack.FluidStack;
import buildcraft.lib.nbt.BcNbt;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Finds every fluid stored anywhere in a captured tile's NBT, whatever key it hides under. The builder has to charge
 * for exactly what {@link SchematicBlockDefault#build} restores, and that restore is a verbatim NBT copy, so the two
 * must be driven off the same scan rather than off hand written paths that go stale when a tile changes its keys.
 * <p>
 * Item component data is skipped: a fluid held by an item (a fragile fluid container in a chest, say) is already paid
 * for as part of that item, so counting it here would charge for it twice.
 */
public final class SchematicFluidNbt {
   private static final int MAX_DEPTH = 32;
   private static final String ITEM_COMPONENTS_KEY = "components";

   private SchematicFluidNbt() {
   }

   /** Every fluid the tile NBT would restore. */
   public static List<FluidStack> collect(@Nullable CompoundTag tileNbt) {
      List<FluidStack> found = new ArrayList<>();
      if (tileNbt != null) {
         walk(tileNbt, found, false, 0);
      }

      return found;
   }

   /** Drops every fluid from the tile NBT, so the tile loads with empty tanks. */
   public static void clear(@Nullable CompoundTag tileNbt) {
      if (tileNbt != null) {
         walk(tileNbt, new ArrayList<>(), true, 0);
      }
   }

   private static void walk(CompoundTag tag, List<FluidStack> found, boolean remove, int depth) {
      if (depth <= MAX_DEPTH) {
         for (String key : List.copyOf(BcNbt.keys(tag))) {
            if (ITEM_COMPONENTS_KEY.equals(key)) {
               continue;
            }

            Tag value = tag.get(key);
            if (value instanceof CompoundTag compound) {
               FluidStack stack = asFluidStack(compound);
               if (stack != null) {
                  found.add(stack);
                  if (remove) {
                     tag.remove(key);
                  }
               } else {
                  walk(compound, found, remove, depth + 1);
               }
            } else if (value instanceof ListTag list && walkList(list, found, remove, depth + 1) && remove) {
               tag.remove(key);
            }
         }
      }
   }

   private static boolean walkList(ListTag list, List<FluidStack> found, boolean remove, int depth) {
      if (depth > MAX_DEPTH) {
         return false;
      }

      boolean holdsFluids = false;

      for (Tag element : list) {
         if (element instanceof CompoundTag compound) {
            FluidStack stack = asFluidStack(compound);
            if (stack != null) {
               found.add(stack);
               holdsFluids = true;
            } else {
               walk(compound, found, remove, depth + 1);
            }
         } else if (element instanceof ListTag nested) {
            holdsFluids |= walkList(nested, found, remove, depth + 1);
         }
      }

      return holdsFluids;
   }

   @Nullable
   private static FluidStack asFluidStack(CompoundTag tag) {
      int amount = BcNbt.getInt(tag, "amount", 0);
      if (amount <= 0) {
         return null;
      }

      String id = BcNbt.getString(tag, "id", "");
      if (id.isEmpty()) {
         return null;
      }

      Identifier fluidId = Identifier.tryParse(id);
      if (fluidId == null) {
         return null;
      }

      Fluid fluid = BcRegistryUtil.getFluid(fluidId);
      return fluid != null && fluid != Fluids.EMPTY ? new FluidStack(fluid, amount) : null;
   }
}
