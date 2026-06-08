/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.lib.fluids.FluidStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public enum RefineryRecipeRegistry implements IRefineryRecipeManager {
   INSTANCE;

   public final IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IDistillationRecipe> distillationRegistry = new RefineryRecipeRegistry.SingleRegistry<>();
   public final IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IHeatableRecipe> heatableRegistry = new RefineryRecipeRegistry.SingleRegistry<>();
   public final IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.ICoolableRecipe> coolableRegistry = new RefineryRecipeRegistry.SingleRegistry<>();

   @Override
   public IRefineryRecipeManager.IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
      return new RefineryRecipeRegistry.HeatableRecipe(in, out, heatFrom, heatTo);
   }

   @Override
   public IRefineryRecipeManager.ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
      return new RefineryRecipeRegistry.CoolableRecipe(in, out, heatFrom, heatTo);
   }

   @Override
   public IRefineryRecipeManager.IDistillationRecipe createDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, long powerRequired) {
      return new RefineryRecipeRegistry.DistillationRecipe(powerRequired, in, outGas, outLiquid);
   }

   @Override
   public IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IHeatableRecipe> getHeatableRegistry() {
      return this.heatableRegistry;
   }

   @Override
   public IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.ICoolableRecipe> getCoolableRegistry() {
      return this.coolableRegistry;
   }

   @Override
   public IRefineryRecipeManager.IRefineryRegistry<IRefineryRecipeManager.IDistillationRecipe> getDistillationRegistry() {
      return this.distillationRegistry;
   }

   public static class CoolableRecipe extends RefineryRecipeRegistry.HeatExchangeRecipe implements IRefineryRecipeManager.ICoolableRecipe {
      public CoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
         super(in, out, heatFrom, heatTo);
      }
   }

   public static class DistillationRecipe extends RefineryRecipeRegistry.RefineryRecipe implements IRefineryRecipeManager.IDistillationRecipe {
      private final FluidStack outGas;
      private final FluidStack outLiquid;
      private final long powerRequired;

      public DistillationRecipe(long powerRequired, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
         super(in);
         this.powerRequired = powerRequired;
         this.outGas = outGas;
         this.outLiquid = outLiquid;
      }

      @Override
      public FluidStack outGas() {
         return this.outGas;
      }

      @Override
      public FluidStack outLiquid() {
         return this.outLiquid;
      }

      @Override
      public long powerRequired() {
         return this.powerRequired;
      }
   }

   public abstract static class HeatExchangeRecipe extends RefineryRecipeRegistry.RefineryRecipe implements IRefineryRecipeManager.IHeatExchangerRecipe {
      private final FluidStack out;
      private final int heatFrom;
      private final int heatTo;

      public HeatExchangeRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
         super(in);
         this.out = out;
         this.heatFrom = heatFrom;
         this.heatTo = heatTo;
      }

      @Override
      public FluidStack out() {
         return this.out;
      }

      @Override
      public int heatFrom() {
         return this.heatFrom;
      }

      @Override
      public int heatTo() {
         return this.heatTo;
      }
   }

   public static class HeatableRecipe extends RefineryRecipeRegistry.HeatExchangeRecipe implements IRefineryRecipeManager.IHeatableRecipe {
      public HeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
         super(in, out, heatFrom, heatTo);
      }
   }

   public abstract static class RefineryRecipe implements IRefineryRecipeManager.IRefineryRecipe {
      private final FluidStack in;

      public RefineryRecipe(FluidStack in) {
         this.in = in;
      }

      @Override
      public FluidStack in() {
         return this.in;
      }
   }

   private static class SingleRegistry<R extends IRefineryRecipeManager.IRefineryRecipe> implements IRefineryRecipeManager.IRefineryRegistry<R> {
      private final List<R> allRecipes = new LinkedList<>();

      @Override
      public Stream<R> getRecipes(Predicate<R> filter) {
         return this.allRecipes.stream().filter(filter);
      }

      @Override
      public Collection<R> getAllRecipes() {
         return this.allRecipes;
      }

      @Nullable
      @Override
      public R getRecipeForInput(@Nullable FluidStack fluid) {
         if (fluid == null) {
            return null;
         }

         for (R recipe : this.allRecipes) {
            if (FluidStack.isSameFluidSameComponents(recipe.in(), fluid)) {
               return recipe;
            }
         }

         return null;
      }

      @Override
      public Collection<R> removeRecipes(Predicate<R> toRemove) {
         List<R> removed = new ArrayList<>();
         Iterator<R> iter = this.allRecipes.iterator();

         while (iter.hasNext()) {
            R recipe = iter.next();
            if (toRemove.test(recipe)) {
               iter.remove();
               removed.add(recipe);
            }
         }

         return removed;
      }

      @Override
      public R addRecipe(R recipe) {
         if (recipe == null) {
            throw new NullPointerException("recipe");
         }

         ListIterator<R> iter = this.allRecipes.listIterator();

         while (iter.hasNext()) {
            R existing = iter.next();
            if (FluidStack.isSameFluidSameComponents(existing.in(), recipe.in())) {
               iter.set(recipe);
               return recipe;
            }
         }

         this.allRecipes.add(recipe);
         return recipe;
      }
   }
}
