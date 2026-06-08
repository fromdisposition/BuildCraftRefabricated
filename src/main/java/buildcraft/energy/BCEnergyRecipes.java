/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.fabric.BCEnergyFluidsFabric;
import buildcraft.lib.fluids.FluidStack;
import buildcraft.lib.misc.MathUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class BCEnergyRecipes {
   private static final int TIME_BASE = 240000;
   private static boolean initialized;

   public static void init() {
      if (!initialized) {
         initialized = true;
         BuildcraftFuelRegistry.coolant.addCoolant(Fluids.WATER, 0.0023F);
         BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.ICE), new FluidStack(Fluids.WATER, 1000), 1.5F);
         BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.PACKED_ICE), new FluidStack(Fluids.WATER, 1000), 2.0F);
         BuildcraftFuelRegistry.coolant.addSolidCoolant(new ItemStack(Blocks.BLUE_ICE), new FluidStack(Fluids.WATER, 1000), 2.5F);
         int _oil = 8;
         int _gas = 16;
         int _light = 4;
         int _dense = 2;
         int _residue = 1;
         int _gas_light = 10;
         int _light_dense = 5;
         int _dense_residue = 2;
         int _gas_light_dense = 8;
         int _light_dense_residue = 3;
         addFuel("fuel_gaseous", 16, 8, 4);
         addFuel("fuel_light", 4, 6, 6);
         addFuel("fuel_dense", 2, 4, 12);
         addFuel("fuel_mixed_light", 10, 3, 5);
         addFuel("fuel_mixed_heavy", 5, 5, 8);
         addDirtyFuel("oil_dense", 2, 4, 4);
         addDirtyFuel("oil_heavy", 3, 2, 4);
         addDirtyFuel("oil", 8, 3, 4);
         addFuel("oil_distilled", 8, 1, 5);
         if (BuildcraftRecipeRegistry.refineryRecipes != null) {
            FluidStack[] gas_light_dense_residue = createFluidStacks("oil", 8);
            FluidStack[] gas_light_dense = createFluidStacks("oil_distilled", 8);
            FluidStack[] gas_light = createFluidStacks("fuel_mixed_light", 10);
            FluidStack[] gas = createFluidStacks("fuel_gaseous", 16);
            FluidStack[] light_dense_residue = createFluidStacks("oil_heavy", 3);
            FluidStack[] light_dense = createFluidStacks("fuel_mixed_heavy", 5);
            FluidStack[] light = createFluidStacks("fuel_light", 4);
            FluidStack[] dense_residue = createFluidStacks("oil_dense", 2);
            FluidStack[] dense = createFluidStacks("fuel_dense", 2);
            FluidStack[] residue = createFluidStacks("oil_residue", 1);
            addDistillation(gas_light_dense_residue, gas, light_dense_residue, 0, 32L * MjAPI.MJ);
            addDistillation(gas_light_dense_residue, gas_light, dense_residue, 1, 16L * MjAPI.MJ);
            addDistillation(gas_light_dense_residue, gas_light_dense, residue, 2, 12L * MjAPI.MJ);
            addDistillation(gas_light_dense, gas, light_dense, 0, 24L * MjAPI.MJ);
            addDistillation(gas_light_dense, gas_light, dense, 1, 16L * MjAPI.MJ);
            addDistillation(gas_light, gas, light, 0, 24L * MjAPI.MJ);
            addDistillation(light_dense_residue, light, dense_residue, 1, 16L * MjAPI.MJ);
            addDistillation(light_dense_residue, light_dense, residue, 2, 12L * MjAPI.MJ);
            addDistillation(light_dense, light, dense, 1, 16L * MjAPI.MJ);
            addDistillation(dense_residue, dense, residue, 2, 12L * MjAPI.MJ);
            addHeatExchange("oil");
            addHeatExchange("oil_residue");
            addHeatExchange("oil_heavy");
            addHeatExchange("oil_dense");
            addHeatExchange("oil_distilled");
            addHeatExchange("fuel_dense");
            addHeatExchange("fuel_mixed_heavy");
            addHeatExchange("fuel_light");
            addHeatExchange("fuel_mixed_light");
            addHeatExchange("fuel_gaseous");
            FluidStack water = new FluidStack(Fluids.WATER, 10);
            BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(water, null, 0, 1);
            FluidStack lava = new FluidStack(Fluids.LAVA, 5);
            BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(lava, null, 4, 2);
         }
      }
   }

   private static Fluid findFluidByHeat(String baseName, int heat) {
      String regName = baseName + (heat == 0 ? "" : "_heat_" + heat);
      Fluid fluid = BCEnergyFluidsFabric.findFluid(baseName, heat);
      if (fluid != null) {
         return fluid;
      }

      for (BCEnergyFluidsFabric.FluidEntry entry : BCEnergyFluidsFabric.ALL) {
         if (entry.name().equals(regName)) {
            return entry.still();
         }
      }

      return null;
   }

   private static Fluid findFluid(String baseName) {
      return findFluidByHeat(baseName, 0);
   }

   private static FluidStack[] createFluidStacks(String baseName, int amount) {
      FluidStack[] arr = new FluidStack[3];

      for (int heat = 0; heat < 3; heat++) {
         Fluid fluid = findFluidByHeat(baseName, heat);
         arr[heat] = fluid != null ? new FluidStack(fluid, amount) : FluidStack.EMPTY;
      }

      return arr;
   }

   private static void addDistillation(FluidStack[] in, FluidStack[] outGas, FluidStack[] outLiquid, int heat, long mjCost) {
      FluidStack _in = in[heat];
      FluidStack _outGas = outGas[heat];
      FluidStack _outLiquid = outLiquid[heat];
      if (!_in.isEmpty() && !_outGas.isEmpty() && !_outLiquid.isEmpty()) {
         IRefineryRecipeManager.IDistillationRecipe existing = BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(_in);
         if (existing == null) {
            int hcf = MathUtil.findHighestCommonFactor(_in.getAmount(), _outGas.getAmount());
            hcf = MathUtil.findHighestCommonFactor(hcf, _outLiquid.getAmount());
            if (hcf > 1) {
               _in = _in.copyWithAmount(_in.getAmount() / hcf);
               _outGas = _outGas.copyWithAmount(_outGas.getAmount() / hcf);
               _outLiquid = _outLiquid.copyWithAmount(_outLiquid.getAmount() / hcf);
               mjCost /= hcf;
            }

            BuildcraftRecipeRegistry.refineryRecipes.addDistillationRecipe(_in, _outGas, _outLiquid, mjCost);
         }
      }
   }

   private static void addFuel(String baseName, int amountDiff, int multiplier, int boostOver4) {
      Fluid fuel = findFluid(baseName);
      if (fuel != null) {
         long powerPerCycle = multiplier * MjAPI.MJ;
         int totalTime = 240000 * boostOver4 / 4 / multiplier / amountDiff;
         BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
      }
   }

   private static void addDirtyFuel(String baseName, int amountDiff, int multiplier, int boostOver4) {
      Fluid fuel = findFluid(baseName);
      if (fuel != null) {
         long powerPerCycle = multiplier * MjAPI.MJ;
         int totalTime = 240000 * boostOver4 / 4 / multiplier / amountDiff;
         Fluid residue = findFluid("oil_residue");
         if (residue == null) {
            BuildcraftFuelRegistry.fuel.addFuel(fuel, powerPerCycle, totalTime);
         } else {
            BuildcraftFuelRegistry.fuel.addDirtyFuel(fuel, powerPerCycle, totalTime, new FluidStack(residue, 1000 / amountDiff));
         }
      }
   }

   private static void addHeatExchange(String baseName) {
      for (int heat = 0; heat < 2; heat++) {
         Fluid cool = findFluidByHeat(baseName, heat);
         Fluid hot = findFluidByHeat(baseName, heat + 1);
         if (cool != null && hot != null) {
            FluidStack coolStack = new FluidStack(cool, 10);
            FluidStack hotStack = new FluidStack(hot, 10);
            BuildcraftRecipeRegistry.refineryRecipes.addHeatableRecipe(coolStack, hotStack, heat, heat + 1);
            BuildcraftRecipeRegistry.refineryRecipes.addCoolableRecipe(hotStack, coolStack, heat + 1, heat);
         }
      }
   }
}
