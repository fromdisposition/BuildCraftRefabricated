/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluids;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class FluidTypes {
   private static final Map<Fluid, FluidType> CACHE = new ConcurrentHashMap<>();

   private FluidTypes() {
   }

   public static void register(Fluid fluid, int viscosity, int density) {
      if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
         CACHE.put(fluid, new FluidType(fluid, viscosity, density));
      }
   }

   public static FluidType of(Fluid fluid) {
      return fluid != null && !fluid.isSame(Fluids.EMPTY) ? CACHE.computeIfAbsent(fluid, FluidType::new) : FluidType.EMPTY;
   }

   public static FluidType of(Holder<Fluid> holder) {
      return of((Fluid)holder.value());
   }

   public static String descriptionIdFor(Fluid fluid) {
      if (fluid == null || fluid.isSame(Fluids.EMPTY)) {
         return "block.minecraft.air";
      }

      Identifier key = BuiltInRegistries.FLUID.getKey(fluid);
      if (key == null) {
         return "fluid_type.minecraft.empty";
      }

      String path = key.getPath();
      if (path.startsWith("flowing_")) {
         key = Identifier.fromNamespaceAndPath(key.getNamespace(), path.substring("flowing_".length()));
      } else if (path.endsWith("_flowing")) {
         key = Identifier.fromNamespaceAndPath(key.getNamespace(), path.substring(0, path.length() - "_flowing".length()));
      }

      if ("minecraft".equals(key.getNamespace())) {
         return key.toLanguageKey("block");
      }

      return key.toLanguageKey("fluid_type");
   }
}
