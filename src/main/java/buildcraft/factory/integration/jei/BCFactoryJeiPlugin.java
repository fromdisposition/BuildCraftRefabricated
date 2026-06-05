/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IRefineryRecipeManager.ICoolableRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.BCFactoryMenuTypes;
import buildcraft.factory.gui.GuiAutoCraftItems;
import buildcraft.factory.gui.GuiDistiller;
import buildcraft.factory.gui.GuiHeatExchange;
import buildcraft.factory.container.ContainerAutoCraftItems;
import buildcraft.lib.integration.jei.BCGhostIngredientHandler;
import buildcraft.lib.integration.jei.BlueprintTransferHandler;

@JeiPlugin
public class BCFactoryJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.parse("buildcraftrefabricated:factory_jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new HeatExchangerCategory(guiHelper));
        registration.addRecipeCategories(new DistillerCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(HeatExchangerRecipeTypes.PAIR, enumerateHeatExchangerPairs());
        registration.addRecipes(DistillerRecipeTypes.DISTILLER, enumerateDistillationRecipes());
    }

    private static List<HeatExchangerRecipePair> enumerateHeatExchangerPairs() {
        List<HeatExchangerRecipePair> pairs = new ArrayList<>();
        for (IHeatableRecipe h : BuildcraftRecipeRegistry.refineryRecipes.getHeatableRegistry().getAllRecipes()) {
            for (ICoolableRecipe c : BuildcraftRecipeRegistry.refineryRecipes.getCoolableRegistry().getAllRecipes()) {
                if (c.heatFrom() > h.heatFrom()) {
                    pairs.add(new HeatExchangerRecipePair(h, c));
                }
            }
        }
        return pairs;
    }

    private static List<IDistillationRecipe> enumerateDistillationRecipes() {
        List<IDistillationRecipe> recipes = new ArrayList<>();
        for (IDistillationRecipe r : BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getAllRecipes()) {
            if (r.in() == null || r.in().isEmpty()) continue;
            boolean hasGas = r.outGas() != null && !r.outGas().isEmpty();
            boolean hasLiquid = r.outLiquid() != null && !r.outLiquid().isEmpty();
            if (!hasGas && !hasLiquid) continue;
            recipes.add(r);
        }
        return recipes;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(
                new BlueprintTransferHandler<>(
                        ContainerAutoCraftItems.class,
                        BCFactoryMenuTypes.AUTO_WORKBENCH_ITEMS
                ),
                RecipeTypes.CRAFTING
        );

        registration.addRecipeTransferHandler(
                new DistillerTransferHandler(registration.getTransferHelper()),
                DistillerRecipeTypes.DISTILLER
        );

        registration.addRecipeTransferHandler(
                new HeatExchangerTransferHandler(registration.getTransferHelper()),
                HeatExchangerRecipeTypes.PAIR
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addRecipeClickArea(
                GuiAutoCraftItems.class,
                90, 47, 23, 10,
                RecipeTypes.CRAFTING
        );

        registration.addGhostIngredientHandler(GuiAutoCraftItems.class, new BCGhostIngredientHandler<>());

        registration.addRecipeClickArea(
                GuiDistiller.class,
                61, 12, 36, 57,
                DistillerRecipeTypes.DISTILLER
        );

        registration.addRecipeClickArea(
                GuiHeatExchange.class,
                73, 36, 30, 21,
                HeatExchangerRecipeTypes.PAIR
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addCraftingStation(RecipeTypes.CRAFTING, BCFactoryItems.AUTOWORKBENCH_ITEM);

        registration.addCraftingStation(HeatExchangerRecipeTypes.PAIR, BCFactoryItems.HEAT_EXCHANGE);

        registration.addCraftingStation(DistillerRecipeTypes.DISTILLER, BCFactoryItems.DISTILLER);
    }
}

