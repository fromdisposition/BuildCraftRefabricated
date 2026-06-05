/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package buildcraft.lib;

import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.lib.fluid.CoolantRegistry;
import buildcraft.lib.fluid.FuelRegistry;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.lib.recipe.RefineryRecipeRegistry;

public final class BCLib {
    public static final String MODID = "buildcraftlib";
    public static final boolean DEV = Boolean.getBoolean("buildcraft.dev");

    private BCLib() {}

    public static void init() {
        initApiRegistries();
        BCLibItems.register();
        buildcraft.lib.fluids.CauldronFluidContent.init();
        buildcraft.lib.fabric.BCLibFakePlayerProvider.register();
        buildcraft.lib.attachments.AttachmentHooks.init();
        buildcraft.lib.registry.MigrationRegistry.init();
        buildcraft.lib.chunkload.ChunkLoaderManager.init();
    }

    private static void initApiRegistries() {
        if (BuildcraftFuelRegistry.fuel == null) {
            BuildcraftFuelRegistry.fuel = FuelRegistry.INSTANCE;
        }
        if (BuildcraftFuelRegistry.coolant == null) {
            BuildcraftFuelRegistry.coolant = CoolantRegistry.INSTANCE;
        }
        if (BuildcraftRecipeRegistry.refineryRecipes == null) {
            BuildcraftRecipeRegistry.refineryRecipes = RefineryRecipeRegistry.INSTANCE;
        }
        if (BuildcraftRecipeRegistry.integrationRecipes == null) {
            BuildcraftRecipeRegistry.integrationRecipes = IntegrationRecipeRegistry.INSTANCE;
        }
    }
}
