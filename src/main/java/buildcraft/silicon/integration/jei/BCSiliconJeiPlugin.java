/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.lib.gui.IGuiElement;
import buildcraft.lib.gui.statement.GuiElementStatementSource;
import buildcraft.lib.integration.jei.BCGhostIngredientHandler;
import buildcraft.lib.integration.jei.BlueprintTransferHandler;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.fabric.integration.jei.BCJeiRecipeTypes;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.gui.GuiGate;
import buildcraft.silicon.gui.GuiIntegrationTable;
import buildcraft.silicon.gui.GuiProgrammingTable;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;

@JeiPlugin
public class BCSiliconJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:silicon_jei_plugin");

   public Identifier getPluginUid() {
      return UID;
   }

   public void registerItemSubtypes(ISubtypeRegistration registration) {
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_LENS, (stack, context) -> {
         DyeColor colour = ItemPluggableLens.getColour(stack);
         boolean isFilter = ItemPluggableLens.isFilter(stack);
         return (colour == null ? "clear" : colour.getName()) + ":" + isFilter;
      });
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_GATE, (stack, context) -> ItemPluggableGate.getVariant(stack).getVariantName());
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_FACADE, (stack, context) -> NBTUtilBC.getItemData(stack).getCompoundOrEmpty("facade"));
   }

   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(
         new IRecipeCategory[]{new AssemblyTableCategory(guiHelper), new IntegrationTableCategory(guiHelper), new ProgrammingTableCategory(guiHelper)}
      );
   }

   public void registerRecipes(IRecipeRegistration registration) {
      registration.addRecipes(BCJeiRecipeTypes.ASSEMBLY, AssemblyRecipeCollector.collect());
      registration.addRecipes(BCJeiRecipeTypes.INTEGRATION, IntegrationRecipeCollector.collect());
      registration.addRecipes(BCJeiRecipeTypes.PROGRAMMING, ProgrammingRecipeCollector.collect());
   }

   @SuppressWarnings("unchecked")
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(
         new BlueprintTransferHandler(ContainerAdvancedCraftingTable.class, BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE), RecipeTypes.CRAFTING
      );
      registration.addRecipeTransferHandler(new AssemblyTableTransferHandler(registration.getTransferHelper()), BCJeiRecipeTypes.ASSEMBLY);
   }

   @SuppressWarnings("unchecked")
   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiAdvancedCraftingTable.class, 93, 32, 23, 16, new IRecipeType[]{RecipeTypes.CRAFTING});
      registration.addGhostIngredientHandler(GuiAdvancedCraftingTable.class, new BCGhostIngredientHandler());
      registration.addRecipeClickArea(GuiAssemblyTable.class, 86, 36, 4, 70, new IRecipeType[]{BCJeiRecipeTypes.ASSEMBLY});
      registration.addRecipeClickArea(GuiIntegrationTable.class, 19, 24, 135, 66, new IRecipeType[]{BCJeiRecipeTypes.INTEGRATION});
      registration.addRecipeClickArea(GuiProgrammingTable.class, 43, 36, 106, 70, new IRecipeType[]{BCJeiRecipeTypes.PROGRAMMING});
      registration.addGuiContainerHandler(GuiGate.class, new IGuiContainerHandler<GuiGate>() {
         public List<Rect2i> getGuiExtraAreas(GuiGate containerScreen) {
            List<Rect2i> extraAreas = new ArrayList<>();

            for (IGuiElement element : containerScreen.mainGui.shownElements) {
               if (element instanceof GuiElementStatementSource) {
                  extraAreas.add(new Rect2i((int)element.getX(), (int)element.getY(), (int)element.getWidth(), (int)element.getHeight()));
               }
            }

            return extraAreas;
         }
      });
   }

   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addCraftingStation(RecipeTypes.CRAFTING, new ItemLike[]{BCSiliconItems.ADVANCED_CRAFTING_TABLE});
      registration.addCraftingStation(BCJeiRecipeTypes.ASSEMBLY, new ItemLike[]{BCSiliconItems.ASSEMBLY_TABLE});
      registration.addCraftingStation(BCJeiRecipeTypes.INTEGRATION, new ItemLike[]{BCSiliconItems.INTEGRATION_TABLE});
      registration.addCraftingStation(BCJeiRecipeTypes.PROGRAMMING, new ItemLike[]{BCSiliconItems.PROGRAMMING_TABLE});
   }
}
