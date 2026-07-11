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
import buildcraft.fabric.integration.jei.BCJeiBootstrap;
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
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

@JeiPlugin
public class BCSiliconJeiPlugin implements IModPlugin {
   private static final Identifier UID = Identifier.parse("buildcraftrefabricated:silicon_jei_plugin");

   @Override
   public Identifier getPluginUid() {
      return UID;
   }

   @Override
   public void registerItemSubtypes(ISubtypeRegistration registration) {
      //? if >= 1.21.10 {
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_LENS, (stack, context) -> {
         DyeColor colour = ItemPluggableLens.getColour(stack);
         boolean isFilter = ItemPluggableLens.isFilter(stack);
         return (colour == null ? "clear" : colour.getName()) + ":" + isFilter;
      });
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_GATE, (stack, context) -> ItemPluggableGate.getVariant(stack).getVariantName());
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_FACADE, (stack, context) -> NBTUtilBC.getItemData(stack).getCompoundOrEmpty("facade"));
      //?} else {
      /*// JEI 19's single-method IIngredientSubtypeInterpreter is deprecated-for-removal; use the current
      // ISubtypeInterpreter overload. (1.21.1 CompoundTag.getCompound also returns the tag directly.)
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_LENS, new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack>() {
         public Object getSubtypeData(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return getLegacyStringSubtypeInfo(stack, context);
         }

         @Deprecated
         public String getLegacyStringSubtypeInfo(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            DyeColor colour = ItemPluggableLens.getColour(stack);
            boolean isFilter = ItemPluggableLens.isFilter(stack);
            return (colour == null ? "clear" : colour.getName()) + ":" + isFilter;
         }
      });
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_GATE, new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack>() {
         public Object getSubtypeData(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return ItemPluggableGate.getVariant(stack).getVariantName();
         }

         @Deprecated
         public String getLegacyStringSubtypeInfo(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return ItemPluggableGate.getVariant(stack).getVariantName();
         }
      });
      registration.registerSubtypeInterpreter(BCSiliconItems.PLUG_FACADE, new mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter<net.minecraft.world.item.ItemStack>() {
         public Object getSubtypeData(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return NBTUtilBC.getItemData(stack).getCompound("facade").toString();
         }

         @Deprecated
         public String getLegacyStringSubtypeInfo(net.minecraft.world.item.ItemStack stack, mezz.jei.api.ingredients.subtypes.UidContext context) {
            return NBTUtilBC.getItemData(stack).getCompound("facade").toString();
         }
      });
      *///?}
   }

   @Override
   public void registerCategories(IRecipeCategoryRegistration registration) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(
         new AssemblyTableCategory(guiHelper), new IntegrationTableCategory(guiHelper), new ProgrammingTableCategory(guiHelper)
      );
   }

   @Override
   public void registerRecipes(IRecipeRegistration registration) {
      BCJeiBootstrap.initSiliconRecipes();
      registration.addRecipes(BCJeiRecipeTypes.ASSEMBLY, AssemblyRecipeCollector.collect());
      registration.addRecipes(BCJeiRecipeTypes.INTEGRATION, IntegrationRecipeCollector.collect());
      registration.addRecipes(BCJeiRecipeTypes.PROGRAMMING, ProgrammingRecipeCollector.collect());
   }

   @Override
   public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
      registration.addRecipeTransferHandler(
         new BlueprintTransferHandler<>(ContainerAdvancedCraftingTable.class, BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE), RecipeTypes.CRAFTING
      );
      registration.addRecipeTransferHandler(new AssemblyTableTransferHandler(registration.getTransferHelper()), BCJeiRecipeTypes.ASSEMBLY);
   }

   @Override
   public void registerGuiHandlers(IGuiHandlerRegistration registration) {
      registration.addRecipeClickArea(GuiAdvancedCraftingTable.class, 93, 36, 22, 15, RecipeTypes.CRAFTING);
      registration.addGhostIngredientHandler(GuiAdvancedCraftingTable.class, new BCGhostIngredientHandler());
      registration.addRecipeClickArea(GuiAssemblyTable.class, 86, 18, 4, 70, BCJeiRecipeTypes.ASSEMBLY);
      registration.addRecipeClickArea(GuiIntegrationTable.class, 84, 48, 41, 10, BCJeiRecipeTypes.INTEGRATION);
      // Spans both arrows flanking the options grid: the upper one (input -> options) at y 22..29 and the lower one
      // (options -> output) at y 76..83, with the arrows' exact x extent 28..38.
      registration.addRecipeClickArea(GuiProgrammingTable.class, 28, 22, 11, 62, BCJeiRecipeTypes.PROGRAMMING);
      registration.addGuiContainerHandler(GuiGate.class, new IGuiContainerHandler<GuiGate>() {
         @Override
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

   @Override
   public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
      registration.addCraftingStation(RecipeTypes.CRAFTING, BCSiliconItems.ADVANCED_CRAFTING_TABLE);
      registration.addCraftingStation(BCJeiRecipeTypes.ASSEMBLY, BCSiliconItems.ASSEMBLY_TABLE);
      registration.addCraftingStation(BCJeiRecipeTypes.INTEGRATION, BCSiliconItems.INTEGRATION_TABLE);
      registration.addCraftingStation(BCJeiRecipeTypes.PROGRAMMING, BCSiliconItems.PROGRAMMING_TABLE);
   }
}
