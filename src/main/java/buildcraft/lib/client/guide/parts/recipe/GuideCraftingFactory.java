/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.IRecipeViewable;

@SuppressWarnings("deprecation")
public class GuideCraftingFactory implements GuidePartFactory {

    private final ChangingItemStack[][] input;
    private final ChangingItemStack output;
    private final int hash;

    public GuideCraftingFactory(ChangingItemStack[][] input, ChangingItemStack output) {
        this.input = input;
        this.output = output;
        int h = 0;
        for (ChangingItemStack[] row : input) {
            for (ChangingItemStack stack : row) {
                h = h * 31 + stack.hashCode();
            }
        }
        this.hash = h * 31 + output.hashCode();
    }

    public boolean outputMatches(ItemStack target) {
        return output.matches(target);
    }

    @Nullable
    public static GuidePartFactory getFactory(CraftingRecipe recipe) {
        if (recipe instanceof IRecipeViewable) {
            return getFactoryFromViewable((IRecipeViewable) recipe);
        }

        if (recipe instanceof ShapedRecipe shaped) {
            return getFactoryFromShaped(shaped);
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            return getFactoryFromShapeless(shapeless);
        }

        return null;
    }

    @Nullable
    public static GuidePartFactory getCyclingFactory(List<CraftingRecipe> recipes) {
        List<GuideCraftingFactory> singles = new ArrayList<>();
        for (CraftingRecipe recipe : recipes) {
            if (getFactory(recipe) instanceof GuideCraftingFactory gcf) {
                singles.add(gcf);
            }
        }
        if (singles.isEmpty()) {
            return null;
        }
        if (singles.size() == 1) {
            return singles.get(0);
        }
        ChangingItemStack[][] input = new ChangingItemStack[3][3];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                List<ItemStack> options = new ArrayList<>(singles.size());
                for (GuideCraftingFactory single : singles) {
                    options.add(firstOption(single.input[x][y]));
                }
                input[x][y] = new ChangingItemStack(options);
            }
        }
        List<ItemStack> outputs = new ArrayList<>(singles.size());
        for (GuideCraftingFactory single : singles) {
            outputs.add(firstOption(single.output));
        }
        return new GuideCraftingFactory(input, new ChangingItemStack(outputs));
    }

    private static ItemStack firstOption(ChangingItemStack stack) {
        List<buildcraft.lib.misc.ItemStackKey> options = stack.getOptions();
        return options.isEmpty() ? ItemStack.EMPTY : options.get(0).baseStack;
    }

    private static GuidePartFactory getFactoryFromShaped(ShapedRecipe recipe) {
        ItemStack output = recipe.assemble(CraftingInput.EMPTY);
        if (output.isEmpty()) return null;

        int width = recipe.getWidth();
        int height = recipe.getHeight();
        List<Optional<Ingredient>> ingredients = recipe.getIngredients();
        int offsetX = width == 1 ? 1 : 0;
        int offsetY = height == 1 ? 1 : 0;

        ChangingItemStack[][] matrix = new ChangingItemStack[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < offsetX || y < offsetY) {
                    matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                    continue;
                }
                int i = (x - offsetX) + (y - offsetY) * width;
                if (i >= ingredients.size() || (x - offsetX) >= width) {
                    matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                } else {
                    Optional<Ingredient> opt = ingredients.get(i);
                    matrix[x][y] = opt.map(GuideCraftingFactory::ingredientToChanging)
                        .orElse(new ChangingItemStack(ItemStack.EMPTY));
                }
            }
        }
        return new GuideCraftingFactory(matrix, new ChangingItemStack(output));
    }

    private static GuidePartFactory getFactoryFromShapeless(ShapelessRecipe recipe) {
        ItemStack output = recipe.assemble(CraftingInput.EMPTY);
        List<Ingredient> ingredients = new ArrayList<>();
        for (Ingredient ingredient : buildcraft.lib.recipe.ShapelessRecipes.ingredients(recipe)) {
            ingredients.add(ingredient);
        }
        if (ingredients.isEmpty() || output.isEmpty()) return null;

        ChangingItemStack[][] matrix = new ChangingItemStack[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int i = x + y * 3;
                if (i < ingredients.size()) {
                    matrix[x][y] = ingredientToChanging(ingredients.get(i));
                } else {
                    matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                }
            }
        }
        return new GuideCraftingFactory(matrix, new ChangingItemStack(output));
    }

    @Nullable
    private static GuidePartFactory getFactoryFromViewable(IRecipeViewable viewable) {
        ChangingItemStack[] inputs = viewable.getRecipeInputs();
        ChangingItemStack outputs = viewable.getRecipeOutputs();
        if (inputs == null || outputs == null) return null;

        int width = 3, height = 3;
        if (viewable instanceof IRecipeViewable.IViewableGrid grid) {
            width = grid.getRecipeWidth();
            height = grid.getRecipeHeight();
        }

        ChangingItemStack[][] matrix = new ChangingItemStack[3][3];
        int offsetX = width == 1 ? 1 : 0;
        int offsetY = height == 1 ? 1 : 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x < offsetX || y < offsetY) {
                    matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                    continue;
                }
                int i = (x - offsetX) + (y - offsetY) * width;
                if (i < inputs.length && (x - offsetX) < width) {
                    matrix[x][y] = inputs[i];
                } else {
                    matrix[x][y] = new ChangingItemStack(ItemStack.EMPTY);
                }
            }
        }
        return new GuideCraftingFactory(matrix, outputs);
    }

    static net.minecraft.util.context.ContextMap displayContext() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        net.minecraft.client.multiplayer.ClientLevel level = mc == null ? null : mc.level;
        if (level != null) {
            return net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level);
        }
        return new net.minecraft.util.context.ContextMap.Builder()
            .create(net.minecraft.world.item.crafting.display.SlotDisplayContext.CONTEXT);
    }

    static ChangingItemStack ingredientToChanging(Ingredient ingredient) {
        net.minecraft.util.context.ContextMap ctx = displayContext();
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : ingredient.display().resolveForStacks(ctx)) {
            if (!stack.isEmpty() && stack.getItem() != net.minecraft.world.item.Items.AIR) {
                stacks.add(stack);
            }
        }
        if (stacks.isEmpty()) {
            return new ChangingItemStack(ItemStack.EMPTY);
        }
        return new ChangingItemStack(stacks);
    }

    @Override
    public GuidePart createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GuideCraftingFactory other = (GuideCraftingFactory) obj;
        if (hash != other.hash) return false;
        if (input.length != other.input.length) return false;
        for (int x = 0; x < input.length; x++) {
            if (input[x].length != other.input[x].length) return false;
            for (int y = 0; y < input[x].length; y++) {
                if (!input[x][y].equals(other.input[x][y])) return false;
            }
        }
        return output.equals(other.output);
    }
}
