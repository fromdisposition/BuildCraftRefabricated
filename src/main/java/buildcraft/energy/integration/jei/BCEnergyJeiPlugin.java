/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.energy.BCEnergyItems;
import buildcraft.energy.BCEnergyRecipeTypes;
import buildcraft.energy.recipe.CombustionFuelRecipe;
import buildcraft.energy.recipe.CoolantRecipe;
import buildcraft.energy.recipe.SolidCoolantRecipe;
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
import net.minecraft.world.item.crafting.RecipeHolder;
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

   private static java.util.Collection<RecipeHolder<?>> allRecipes() {
      var srv = Minecraft.getInstance().getSingleplayerServer();
      return srv != null ? srv.getRecipeManager().getRecipes() : java.util.List.of();
   }

   private static List<CombustionFuelRecipe> collectCombustionFuels() {
      List<CombustionFuelRecipe> fuels = new ArrayList<>();
      for (RecipeHolder<?> h : allRecipes()) {
         if (h.value() instanceof CombustionFuelRecipe r) fuels.add(r);
      }
      return fuels;
   }

   private static List<CombustionCoolantJei> collectCoolants() {
      List<CombustionCoolantJei> out = new ArrayList<>();
      for (RecipeHolder<?> h : allRecipes()) {
         if (h.value() instanceof CoolantRecipe r) {
            out.add(new CombustionCoolantJei(ItemStack.EMPTY, new FluidStack(r.fluid(), 1000), r.degreesCoolingPerMb()));
         } else if (h.value() instanceof SolidCoolantRecipe r) {
            out.add(new CombustionCoolantJei(new ItemStack(r.item()), new FluidStack(r.coolantFluid(), r.coolantAmountPerItem()), 0.0F));
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
