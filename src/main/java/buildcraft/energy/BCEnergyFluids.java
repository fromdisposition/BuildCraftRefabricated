/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.fabric.BCEnergyFluidsFabric;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class BCEnergyFluids {
   public static final List<String> BASE_NAMES = BCEnergyFluidsFabric.BASE_NAMES;
   public static List<BCEnergyFluids.FluidEntry> ALL = List.of();
   public static BCEnergyFluids.FluidEntry OIL_COOL;

   private BCEnergyFluids() {
   }

   public static int getHeat(Fluid fluid) {
      return BCEnergyFluidsFabric.getHeat(fluid);
   }

   public static String getBaseName(Fluid fluid) {
      return BCEnergyFluidsFabric.getBaseName(fluid);
   }

   public static void refreshSnapshot() {
      ALL = BCEnergyFluidsFabric.ALL
         .stream()
         .map(
            e -> new BCEnergyFluids.FluidEntry(
               e.name(), e.baseName(), e.heat(), 0, 0, 0, e.gaseous(), e.tintColor(), e.texLight(), e.texDark(), e.still(), e.flowing(), e.block(), e.bucket()
            )
         )
         .toList();
      OIL_COOL = ALL.stream().filter(e -> "oil".equals(e.baseName()) && e.heat() == 0).findFirst().orElse(null);
   }

   public record FluidEntry(
      String name,
      String baseName,
      int heat,
      int density,
      int viscosity,
      int temperature,
      boolean gaseous,
      int tintColor,
      int texLight,
      int texDark,
      Fluid source,
      Fluid flowing,
      Block block,
      Item bucket
   ) {
   }
}
