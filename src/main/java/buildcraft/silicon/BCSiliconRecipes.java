/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import buildcraft.lib.recipe.DataComponentIngredient;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipeBasic;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.core.BCCoreBlocks;

import buildcraft.lib.misc.ColourUtil;
import buildcraft.lib.recipe.AssemblyRecipeRegistry;

import buildcraft.silicon.gate.EnumGateLogic;
import buildcraft.silicon.gate.EnumGateMaterial;
import buildcraft.silicon.gate.EnumGateModifier;
import buildcraft.silicon.gate.GateVariant;
import buildcraft.silicon.recipe.FacadeAssemblyRecipes;

import buildcraft.transport.BCTransportItems;

@SuppressWarnings("deprecation")
public class BCSiliconRecipes {
    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerPlugRecipes();
        registerGateAssemblyRecipes();
        registerChipsetRecipes();
        registerLensRecipes();
        registerWireRecipes();
        registerGateCopierRecipe();
        registerFacadeRecipes();
    }

    private static void registerPlugRecipes() {

        {
            Set<IngredientStack> input = new HashSet<>();
            input.add(new IngredientStack(Ingredient.of(BCCoreBlocks.ENGINE_REDSTONE)));
            input.add(new IngredientStack(Ingredient.of(Items.IRON_INGOT), 2));
            ItemStack output = new ItemStack(BCSiliconItems.PLUG_PULSAR.get());
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic("plug_pulsar", 1000 * MjAPI.MJ, input, output));
        }

        {
            ImmutableSet<IngredientStack> input = ImmutableSet.of(
                new IngredientStack(Ingredient.of(Blocks.DAYLIGHT_DETECTOR)));
            ItemStack output = new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR.get());
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic("light-sensor", 500 * MjAPI.MJ, input, output));
        }

        {
            ImmutableSet<IngredientStack> input = ImmutableSet.of(
                new IngredientStack(Ingredient.of(Items.CLOCK)));
            ItemStack output = new ItemStack(BCSiliconItems.PLUG_TIMER.get());
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic("timer", 500 * MjAPI.MJ, input, output));
        }
    }

    private static void registerGateAssemblyRecipes() {

        makeGateAssembly(20_000, EnumGateMaterial.IRON, EnumGateModifier.NO_MODIFIER,
            BCSiliconItems.CHIPSET_IRON.get());
        makeGateAssembly(40_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.NO_MODIFIER,
            BCSiliconItems.CHIPSET_IRON.get(),
            new IngredientStack(Ingredient.of(Blocks.NETHER_BRICKS)));
        makeGateAssembly(80_000, EnumGateMaterial.GOLD, EnumGateModifier.NO_MODIFIER,
            BCSiliconItems.CHIPSET_GOLD.get());

        IngredientStack lapis = new IngredientStack(Ingredient.of(Items.LAPIS_LAZULI));
        makeGateModifierAssembly(40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_QUARTZ.get())));
        makeGateModifierAssembly(80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_DIAMOND.get())));

        makeGateModifierAssembly(80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_QUARTZ.get())));
        makeGateModifierAssembly(120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_DIAMOND.get())));

        makeGateModifierAssembly(100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
        makeGateModifierAssembly(140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_QUARTZ.get())));
        makeGateModifierAssembly(180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND,
            new IngredientStack(Ingredient.of(BCSiliconItems.CHIPSET_DIAMOND.get())));
    }

    private static void registerChipsetRecipes() {
        ImmutableSet<IngredientStack> input;

        input = ImmutableSet.of(new IngredientStack(Ingredient.of(Items.REDSTONE)));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("chipset_redstone",
            10_000 * MjAPI.MJ, input, new ItemStack(BCSiliconItems.CHIPSET_REDSTONE.get())));

        input = ImmutableSet.of(
            new IngredientStack(Ingredient.of(Items.REDSTONE)),
            new IngredientStack(Ingredient.of(Items.IRON_INGOT)));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("chipset_iron",
            20_000 * MjAPI.MJ, input, new ItemStack(BCSiliconItems.CHIPSET_IRON.get())));

        input = ImmutableSet.of(
            new IngredientStack(Ingredient.of(Items.REDSTONE)),
            new IngredientStack(Ingredient.of(Items.GOLD_INGOT)));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("chipset_gold",
            40_000 * MjAPI.MJ, input, new ItemStack(BCSiliconItems.CHIPSET_GOLD.get())));

        input = ImmutableSet.of(
            new IngredientStack(Ingredient.of(Items.REDSTONE)),
            new IngredientStack(Ingredient.of(Items.QUARTZ)));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("chipset_quartz",
            60_000 * MjAPI.MJ, input, new ItemStack(BCSiliconItems.CHIPSET_QUARTZ.get())));

        input = ImmutableSet.of(
            new IngredientStack(Ingredient.of(Items.REDSTONE)),
            new IngredientStack(Ingredient.of(Items.DIAMOND)));
        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic("chipset_diamond",
            80_000 * MjAPI.MJ, input, new ItemStack(BCSiliconItems.CHIPSET_DIAMOND.get())));
    }

    private static void registerLensRecipes() {
        for (DyeColor colour : ColourUtil.COLOURS) {
            String name = String.format("lens-regular-%s", colour.getName());
            Block stainedGlass = getStainedGlass(colour);
            IngredientStack stainedGlassIngredient = new IngredientStack(Ingredient.of(stainedGlass));
            ImmutableSet<IngredientStack> input = ImmutableSet.of(stainedGlassIngredient);
            ItemStack output = BCSiliconItems.PLUG_LENS.get().getStack(colour, false);
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));

            name = String.format("lens-filter-%s", colour.getName());
            output = BCSiliconItems.PLUG_LENS.get().getStack(colour, true);
            input = ImmutableSet.of(stainedGlassIngredient,
                new IngredientStack(Ingredient.of(Blocks.IRON_BARS)));
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic(name, 500 * MjAPI.MJ, input, output));
        }

        IngredientStack glass = new IngredientStack(Ingredient.of(Blocks.GLASS));
        ImmutableSet<IngredientStack> input = ImmutableSet.of(glass);
        ItemStack output = BCSiliconItems.PLUG_LENS.get().getStack(null, false);
        AssemblyRecipeRegistry.register(
            new AssemblyRecipeBasic("lens-regular", 500 * MjAPI.MJ, input, output));

        output = BCSiliconItems.PLUG_LENS.get().getStack(null, true);
        input = ImmutableSet.of(glass, new IngredientStack(Ingredient.of(Blocks.IRON_BARS)));
        AssemblyRecipeRegistry.register(
            new AssemblyRecipeBasic("lens-filter", 500 * MjAPI.MJ, input, output));
    }

    private static void registerWireRecipes() {

        for (DyeColor colour : ColourUtil.COLOURS) {
            String name = String.format("wire-%s", colour.getName());
            ImmutableSet<IngredientStack> input = ImmutableSet.of(
                new IngredientStack(Ingredient.of(getDyeItem(colour))),
                new IngredientStack(Ingredient.of(Items.REDSTONE)),
                new IngredientStack(Ingredient.of(Items.IRON_INGOT)));
            ItemStack output = new ItemStack(BCTransportItems.WIRE_ITEMS.get(colour).get(), 8);
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic(name, 5_000 * MjAPI.MJ, input, output));
        }
    }

    private static net.minecraft.world.item.Item getDyeItem(DyeColor colour) {
        return switch (colour) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }

    private static void registerGateCopierRecipe() {
        ImmutableSet.Builder<IngredientStack> input = ImmutableSet.builder();

        input.add(new IngredientStack(Ingredient.of(Items.STICK)));
        input.add(new IngredientStack(Ingredient.of(Items.IRON_INGOT)));
        input.add(new IngredientStack(Ingredient.of(Items.REDSTONE)));
        input.add(new IngredientStack(Ingredient.of(Items.REDSTONE)));
        input.add(new IngredientStack(Ingredient.of(Items.GOLD_INGOT)));

        AssemblyRecipeRegistry.register(new AssemblyRecipeBasic(
            "gate_copier", 500 * MjAPI.MJ, input.build(),
            new ItemStack(BCSiliconItems.GATE_COPIER.get())));
    }

    private static void registerFacadeRecipes() {
        AssemblyRecipeRegistry.register(FacadeAssemblyRecipes.INSTANCE);
    }

    private static void makeGateAssembly(int multiplier, EnumGateMaterial material,
        EnumGateModifier modifier, net.minecraft.world.item.Item chipset,
        IngredientStack... additional) {
        ImmutableSet.Builder<IngredientStack> temp = ImmutableSet.builder();
        temp.add(new IngredientStack(Ingredient.of(chipset)));
        for (IngredientStack add : additional) {
            temp.add(add);
        }
        ImmutableSet<IngredientStack> input = temp.build();

        String name = String.format("gate-and-%s-%s", material, modifier);
        ItemStack output = BCSiliconItems.PLUG_GATE.get()
            .getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
        AssemblyRecipeRegistry.register(
            new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output));

        name = String.format("gate-or-%s-%s", material, modifier);
        output = BCSiliconItems.PLUG_GATE.get()
            .getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
        AssemblyRecipeRegistry.register(
            new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output));
    }

    private static void makeGateModifierAssembly(int multiplier, EnumGateMaterial material,
        EnumGateModifier modifier, IngredientStack... mods) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            String name = String.format("gate-modifier-%s-%s-%s", logic, material, modifier);
            GateVariant variantFrom = new GateVariant(logic, material, EnumGateModifier.NO_MODIFIER);
            ItemStack toUpgrade = BCSiliconItems.PLUG_GATE.get().getStack(variantFrom);
            ItemStack output = BCSiliconItems.PLUG_GATE.get()
                .getStack(new GateVariant(logic, material, modifier));
            ImmutableSet.Builder<IngredientStack> inputBuilder = ImmutableSet.builder();

            inputBuilder.add(new IngredientStack(DataComponentIngredient.of(toUpgrade)));

            for (IngredientStack mod : mods) {
                inputBuilder.add(mod);
            }
            ImmutableSet<IngredientStack> input = inputBuilder.build();
            AssemblyRecipeRegistry.register(
                new AssemblyRecipeBasic(name, MjAPI.MJ * multiplier, input, output));
        }
    }

    private static Block getStainedGlass(DyeColor colour) {
        return switch (colour) {
            case WHITE -> Blocks.WHITE_STAINED_GLASS;
            case ORANGE -> Blocks.ORANGE_STAINED_GLASS;
            case MAGENTA -> Blocks.MAGENTA_STAINED_GLASS;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_STAINED_GLASS;
            case YELLOW -> Blocks.YELLOW_STAINED_GLASS;
            case LIME -> Blocks.LIME_STAINED_GLASS;
            case PINK -> Blocks.PINK_STAINED_GLASS;
            case GRAY -> Blocks.GRAY_STAINED_GLASS;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_STAINED_GLASS;
            case CYAN -> Blocks.CYAN_STAINED_GLASS;
            case PURPLE -> Blocks.PURPLE_STAINED_GLASS;
            case BLUE -> Blocks.BLUE_STAINED_GLASS;
            case BROWN -> Blocks.BROWN_STAINED_GLASS;
            case GREEN -> Blocks.GREEN_STAINED_GLASS;
            case RED -> Blocks.RED_STAINED_GLASS;
            case BLACK -> Blocks.BLACK_STAINED_GLASS;
        };
    }
}
