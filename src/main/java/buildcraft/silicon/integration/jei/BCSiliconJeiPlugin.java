/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import buildcraft.lib.integration.jei.BCGhostIngredientHandler;
import buildcraft.lib.integration.jei.BlueprintTransferHandler;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.BCSiliconMenuTypes;
import buildcraft.silicon.container.ContainerAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAdvancedCraftingTable;
import buildcraft.silicon.gui.GuiAssemblyTable;
import buildcraft.silicon.item.ItemPluggableGate;
import buildcraft.silicon.item.ItemPluggableLens;

@JeiPlugin
public class BCSiliconJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.parse("buildcraftrefabricated:silicon_jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {

        registration.registerSubtypeInterpreter(
                BCSiliconItems.PLUG_LENS.get(),
                (stack, context) -> {
                    DyeColor colour = ItemPluggableLens.getColour(stack);
                    boolean isFilter = ItemPluggableLens.isFilter(stack);
                    return (colour == null ? "clear" : colour.getName()) + ":" + isFilter;
                }
        );

        registration.registerSubtypeInterpreter(
                BCSiliconItems.PLUG_GATE.get(),
                (stack, context) -> ItemPluggableGate.getVariant(stack).getVariantName()
        );

        registration.registerSubtypeInterpreter(
                BCSiliconItems.PLUG_FACADE.get(),
                (stack, context) -> NBTUtilBC.getItemData(stack).getCompoundOrEmpty("facade")
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new AssemblyTableCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(AssemblyRecipeJeiTypes.ASSEMBLY, AssemblyRecipeCollector.collect());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(
                new BlueprintTransferHandler<>(
                        ContainerAdvancedCraftingTable.class,
                        BCSiliconMenuTypes.ADVANCED_CRAFTING_TABLE
                ),
                RecipeTypes.CRAFTING
        );

        registration.addRecipeTransferHandler(
                new AssemblyTableTransferHandler(registration.getTransferHelper()),
                AssemblyRecipeJeiTypes.ASSEMBLY
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addRecipeClickArea(
                GuiAdvancedCraftingTable.class,
                93, 32, 23, 16,
                RecipeTypes.CRAFTING
        );

        registration.addGhostIngredientHandler(GuiAdvancedCraftingTable.class, new BCGhostIngredientHandler<>());

        registration.addRecipeClickArea(
                GuiAssemblyTable.class,
                86, 36, 4, 70,
                AssemblyRecipeJeiTypes.ASSEMBLY
        );

        registration.addGuiContainerHandler(buildcraft.silicon.gui.GuiGate.class, new mezz.jei.api.gui.handlers.IGuiContainerHandler<buildcraft.silicon.gui.GuiGate>() {
            @Override
            public java.util.List<net.minecraft.client.renderer.Rect2i> getGuiExtraAreas(buildcraft.silicon.gui.GuiGate containerScreen) {
                java.util.List<net.minecraft.client.renderer.Rect2i> extraAreas = new java.util.ArrayList<>();
                for (buildcraft.lib.gui.IGuiElement element : containerScreen.mainGui.shownElements) {
                    if (element instanceof buildcraft.lib.gui.statement.GuiElementStatementSource) {
                        extraAreas.add(new net.minecraft.client.renderer.Rect2i(
                                (int) element.getX(), (int) element.getY(),
                                (int) element.getWidth(), (int) element.getHeight()
                        ));
                    }
                }
                return extraAreas;
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addCraftingStation(RecipeTypes.CRAFTING, BCSiliconItems.ADVANCED_CRAFTING_TABLE.get());

        registration.addCraftingStation(AssemblyRecipeJeiTypes.ASSEMBLY, BCSiliconItems.ASSEMBLY_TABLE.get());
    }
}

