/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.container.ContainerAutoCraftFluids;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.factory.gui.GuiAutoCraftFluids;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.lib.integration.jei.BCGhostIngredientHandler;
import buildcraft.lib.integration.jei.BlueprintTransferHandler;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class BCFactoryJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:factory_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(new IRecipeCategory[]{new HeatExchangerCategory(guiHelper)});
      registration.addRecipeCategories(new IRecipeCategory[]{new DistillerCategory(guiHelper)});
   }

   public void registerRecipes(IRecipeRegistration registration) {
      BCJeiBootstrap.initEnergyRecipes();
      registration.addRecipes(BCJeiRecipeTypes.HEAT_EXCHANGER, enumerateHeatExchangerPairs());
      registration.addRecipes(BCJeiRecipeTypes.DISTILLER, enumerateDistillationRecipes());
   }

   private static List<HeatExchangerRecipePair> enumerateHeatExchangerPairs() {
      List<HeatExchangerRecipePair> pairs = new ArrayList<>();
      if (BuildcraftRecipeRegistry.refineryRecipes == null) {
         return pairs;
      }

      for (IRefineryRecipeManager.IHeatableRecipe h : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
         for (IRefineryRecipeManager.ICoolableRecipe c : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
            if (c.heatFrom() > h.heatFrom()) {
               pairs.add(new HeatExchangerRecipePair(h, c));
            }
         }
      }

      return pairs;
   }

   private static List<IRefineryRecipeManager.IDistillationRecipe> enumerateDistillationRecipes() {
      List<IRefineryRecipeManager.IDistillationRecipe> recipes = new ArrayList<>();
      if (BuildcraftRecipeRegistry.refineryRecipes == null) {
         return recipes;
      }

      for (IRefineryRecipeManager.IDistillationRecipe r : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
         if (r.in() != null && !r.in().isEmpty()) {
            boolean hasGas = r.outGas() != null && !r.outGas().isEmpty();
            boolean hasLiquid = r.outLiquid() != null && !r.outLiquid().isEmpty();
            if (hasGas || hasLiquid) {
               recipes.add(r);
            }
         }
      }

      return recipes;
   }

   @SuppressWarnings("unchecked")
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(
         new BlueprintTransferHandler(ContainerAutoCraftItems.class, BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS), RecipeTypes.CRAFTING
      );
      if (BCFactoryMenuTypes.AUTO_WORKBENCH_FLUIDS != null) {
         registration.addRecipeTransferHandler(
            new BlueprintTransferHandler(ContainerAutoCraftFluids.class, BCFactoryMenuTypes.AUTO_WORKBENCH_FLUIDS), RecipeTypes.CRAFTING
         );
      }

      registration.addRecipeTransferHandler(new DistillerTransferHandler(registration.getTransferHelper()), BCJeiRecipeTypes.DISTILLER);
      registration.addRecipeTransferHandler(new HeatExchangerTransferHandler(registration.getTransferHelper()), BCJeiRecipeTypes.HEAT_EXCHANGER);
   }

   @SuppressWarnings("unchecked")
   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiAutoCraftItems.class, 90, 47, 23, 10, new IRecipeType[]{RecipeTypes.CRAFTING});
      if (BCFactoryMenuTypes.AUTO_WORKBENCH_FLUIDS != null) {
         registration.addRecipeClickArea(GuiAutoCraftFluids.class, 90, 47, 23, 10, new IRecipeType[]{RecipeTypes.CRAFTING});
         registration.addGhostIngredientHandler(GuiAutoCraftFluids.class, new BCGhostIngredientHandler());
      }

      registration.addGhostIngredientHandler(GuiAutoCraftItems.class, new BCGhostIngredientHandler());
      registration.addRecipeClickArea(GuiDistiller.class, 61, 12, 36, 57, new IRecipeType[]{BCJeiRecipeTypes.DISTILLER});
      registration.addRecipeClickArea(GuiHeatExchange.class, 73, 36, 30, 21, new IRecipeType[]{BCJeiRecipeTypes.HEAT_EXCHANGER});
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      if (BCFactoryItems.AUTOWORKBENCH_FLUID != null) {
         registration.addCraftingStation(RecipeTypes.CRAFTING, new ItemLike[]{BCFactoryItems.AUTOWORKBENCH_ITEM, BCFactoryItems.AUTOWORKBENCH_FLUID});
      } else {
         registration.addCraftingStation(RecipeTypes.CRAFTING, new ItemLike[]{BCFactoryItems.AUTOWORKBENCH_ITEM});
      }
      registration.addCraftingStation(BCJeiRecipeTypes.HEAT_EXCHANGER, new ItemLike[]{BCFactoryItems.HEAT_EXCHANGE});
      registration.addCraftingStation(BCJeiRecipeTypes.DISTILLER, new ItemLike[]{BCFactoryItems.DISTILLER});
   }
}
