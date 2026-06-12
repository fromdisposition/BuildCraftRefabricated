/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.ICoolant;
import buildcraft.api.fuels.IFuel;
import buildcraft.api.fuels.ISolidCoolant;
import buildcraft.energy.BCEnergyItems;
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.energy.client.gui.GuiEngineIron_BC8;
import buildcraft.energy.client.gui.GuiEngineStone_BC8;
import buildcraft.lib.fluid.stack.FluidStack;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FuelValues;

@JeiPlugin
public class BCEnergyJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:energy_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(
         new CombustionFuelCategory(guiHelper), new CombustionCoolantCategory(guiHelper), new StirlingFuelCategory(guiHelper)
      );
   }

   public void registerRecipes(IRecipeRegistration registration) {
      BCJeiBootstrap.initEnergyRecipes();
      registration.addRecipes(BCJeiRecipeTypes.COMBUSTION_FUEL, collectCombustionFuels());
      registration.addRecipes(BCJeiRecipeTypes.COMBUSTION_COOLANT, collectCoolants());
      registration.addRecipes(BCJeiRecipeTypes.STIRLING_FUEL, collectStirlingFuels());
   }

   private static List<IFuel> collectCombustionFuels() {
      List<IFuel> fuels = new ArrayList<>();
      if (BuildcraftFuelRegistry.fuel == null) {
         return fuels;
      }

      for (IFuel fuel : BuildcraftFuelRegistry.fuel.getFuels()) {
         FluidStack fluid = fuel.getFluid();
         if (fluid != null && !fluid.isEmpty()) {
            fuels.add(fuel);
         }
      }

      return fuels;
   }

   private static List<CombustionCoolantJei> collectCoolants() {
      List<CombustionCoolantJei> out = new ArrayList<>();
      if (BuildcraftFuelRegistry.coolant == null) {
         return out;
      }

      for (ICoolant coolant : BuildcraftFuelRegistry.coolant.getCoolants()) {
         FluidStack rep = coolant.getRepresentativeFluid();
         if (rep != null && !rep.isEmpty()) {
            out.add(new CombustionCoolantJei(ItemStack.EMPTY, rep, coolant.getDegreesCoolingPerMB(rep, 1.0F)));
         }
      }

      for (ISolidCoolant solid : BuildcraftFuelRegistry.coolant.getSolidCoolants()) {
         ItemStack rep = solid.getRepresentativeStack();
         if (rep != null && !rep.isEmpty()) {
            FluidStack produced = solid.getFluidFromSolidCoolant(rep);
            out.add(new CombustionCoolantJei(rep, produced == null ? FluidStack.EMPTY : produced, 0.0F));
         }
      }

      return out;
   }

   private static List<StirlingFuelJei> collectStirlingFuels() {
      List<StirlingFuelJei> out = new ArrayList<>();
      Level level = Minecraft.getInstance().level;
      if (level == null) {
         return out;
      }

      FuelValues fuelValues = level.fuelValues();

      for (Item item : fuelValues.fuelItems()) {
         ItemStack stack = new ItemStack(item);
         int burnTime = fuelValues.burnDuration(stack);
         if (burnTime > 0) {
            out.add(new StirlingFuelJei(stack, burnTime));
         }
      }

      return out;
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addCraftingStation(BCJeiRecipeTypes.COMBUSTION_FUEL, new ItemLike[]{BCEnergyItems.ENGINE_IRON});
      registration.addCraftingStation(BCJeiRecipeTypes.COMBUSTION_COOLANT, new ItemLike[]{BCEnergyItems.ENGINE_IRON});
      registration.addCraftingStation(BCJeiRecipeTypes.STIRLING_FUEL, new ItemLike[]{BCEnergyItems.ENGINE_STONE});
   }

   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiEngineStone_BC8.class, 81, 25, 14, 14, new IRecipeType[]{BCJeiRecipeTypes.STIRLING_FUEL});
      registration.addRecipeClickArea(
         GuiEngineIron_BC8.class, 44, 22, 34, 52, new IRecipeType[]{BCJeiRecipeTypes.COMBUSTION_FUEL, BCJeiRecipeTypes.COMBUSTION_COOLANT}
      );
   }
}
