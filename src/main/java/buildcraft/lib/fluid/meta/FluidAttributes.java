/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.fluid.meta;

import buildcraft.lib.fluid.stack.FluidStack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class FluidAttributes {
   public static final int BUCKET_VOLUME = 1000;
   public static final FluidAttributes EMPTY = new FluidAttributes(Fluids.EMPTY, 1000, 1000);
   private static final Map<Fluid, FluidAttributes> CACHE = new ConcurrentHashMap<>();

   private final Fluid fluid;
   private final String descriptionId;
   private final int viscosity;
   private final int density;

   private FluidAttributes(Fluid fluid) {
      this(fluid, defaultViscosity(fluid), defaultDensity(fluid));
   }

   private FluidAttributes(Fluid fluid, int viscosity, int density) {
      this.fluid = fluid;
      this.viscosity = viscosity;
      this.density = density;
      this.descriptionId = descriptionIdFor(fluid);
   }

   public static void register(Fluid fluid, int viscosity, int density) {
      if (fluid != null && !fluid.isSame(Fluids.EMPTY)) {
         CACHE.put(fluid, new FluidAttributes(fluid, viscosity, density));
      }
   }

   public static FluidAttributes of(Fluid fluid) {
      return fluid != null && !fluid.isSame(Fluids.EMPTY) ? CACHE.computeIfAbsent(fluid, FluidAttributes::new) : EMPTY;
   }

   public static FluidAttributes of(Holder<Fluid> holder) {
      return of(holder.value());
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

   public static Component displayName(Fluid fluid) {
      return fluid == null || fluid.isSame(Fluids.EMPTY) ? Component.empty() : Component.translatable(descriptionIdFor(fluid));
   }

   private static int defaultViscosity(Fluid fluid) {
      return fluid != Fluids.LAVA && fluid != Fluids.FLOWING_LAVA ? 1000 : 3000;
   }

   private static int defaultDensity(Fluid fluid) {
      return 1000;
   }

   public Fluid getFluid() {
      return this.fluid;
   }

   public int getViscosity() {
      return this.viscosity;
   }

   public int getViscosity(FluidStack stack) {
      return this.getViscosity();
   }

   public int getDensity() {
      return this.density;
   }

   public final boolean isLighterThanAir() {
      return this.density <= 0;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public String getDescriptionId(FluidStack stack) {
      return this.getDescriptionId();
   }

   public @Nullable Item getBucket(FluidStack stack) {
      return this.fluid.getBucket();
   }

   @Override
   public String toString() {
      return this.descriptionId;
   }
}
