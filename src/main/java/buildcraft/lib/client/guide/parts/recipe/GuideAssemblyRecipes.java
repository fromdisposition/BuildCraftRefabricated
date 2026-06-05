package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;

@SuppressWarnings("deprecation")
public enum GuideAssemblyRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (buildcraft.api.recipes.AssemblyRecipe recipe : buildcraft.lib.recipe.AssemblyRecipeRegistry.REGISTRY.values()) {
            for (ItemStack out : recipe.getOutputPreviews()) {
                boolean isUsed = false;
                for (buildcraft.api.recipes.IngredientStack ing : recipe.getInputsFor(out)) {
                    if (ing.ingredient.test(stack)) {
                        isUsed = true;
                        break;
                    }
                }
                if (isUsed) {
                    list.add(createFactory(recipe, out));
                }
            }
        }
        return list;
    }

    @Override
    public List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();
        for (buildcraft.api.recipes.AssemblyRecipe recipe : buildcraft.lib.recipe.AssemblyRecipeRegistry.REGISTRY.values()) {
            for (ItemStack out : recipe.getOutputPreviews()) {
                if (ItemStack.isSameItem(stack, out)) {
                    list.add(createFactory(recipe, out));
                }
            }
        }
        return list;
    }

    @Nullable
    public static GuidePartFactory getFactoryByName(String name) {
        buildcraft.api.recipes.AssemblyRecipe recipe = buildcraft.lib.recipe.AssemblyRecipeRegistry.REGISTRY.get(name);
        if (recipe == null) {
            return null;
        }
        for (ItemStack out : recipe.getOutputPreviews()) {
            return createFactory(recipe, out);
        }
        return null;
    }

    private static GuidePartFactory createFactory(buildcraft.api.recipes.AssemblyRecipe recipe, ItemStack output) {
        return new GuideAssemblyFactory(resolveInputStacks(recipe, output), output,
            recipe.getRequiredMicroJoulesFor(output));
    }

    private static ItemStack[] resolveInputStacks(buildcraft.api.recipes.AssemblyRecipe recipe, ItemStack output) {
        java.util.Set<buildcraft.api.recipes.IngredientStack> inputs = recipe.getInputsFor(output);
        net.minecraft.util.context.ContextMap ctx = GuideCraftingFactory.displayContext();
        ItemStack[] inStacks = new ItemStack[inputs.size()];
        int i = 0;
        for (buildcraft.api.recipes.IngredientStack ing : inputs) {
            java.util.List<ItemStack> resolved = ing.ingredient.display().resolveForStacks(ctx);
            ItemStack first = ItemStack.EMPTY;
            for (ItemStack candidate : resolved) {
                if (!candidate.isEmpty() && candidate.getItem() != net.minecraft.world.item.Items.AIR) {
                    first = candidate;
                    break;
                }
            }
            if (!first.isEmpty()) {
                ItemStack rep = first.copy();
                rep.setCount(ing.count);
                inStacks[i++] = rep;
            } else {
                inStacks[i++] = ItemStack.EMPTY;
            }
        }
        return inStacks;
    }

    public static List<buildcraft.api.recipes.AssemblyRecipe> gatherByNameMatch(String substring) {
        List<buildcraft.api.recipes.AssemblyRecipe> matched = new ArrayList<>();
        for (java.util.Map.Entry<String, buildcraft.api.recipes.AssemblyRecipe> entry
                : new java.util.TreeMap<>(buildcraft.lib.recipe.AssemblyRecipeRegistry.REGISTRY).entrySet()) {
            if (entry.getKey().contains(substring)) {
                matched.add(entry.getValue());
            }
        }
        return matched;
    }

    @Nullable
    public static GuidePartFactory getCyclingFactoryByNameMatch(String substring) {
        return getCyclingFactory(gatherByNameMatch(substring));
    }

    @Nullable
    public static GuidePartFactory getCyclingFactory(List<buildcraft.api.recipes.AssemblyRecipe> recipes) {
        List<ItemStack[]> inputsPerRecipe = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        List<Long> mjCosts = new ArrayList<>();
        int maxSlots = 0;
        for (buildcraft.api.recipes.AssemblyRecipe recipe : recipes) {
            ItemStack output = ItemStack.EMPTY;
            for (ItemStack out : recipe.getOutputPreviews()) {
                output = out;
                break;
            }
            if (output.isEmpty()) {
                continue;
            }
            ItemStack[] inStacks = resolveInputStacks(recipe, output);
            inputsPerRecipe.add(inStacks);
            outputs.add(output);
            mjCosts.add(recipe.getRequiredMicroJoulesFor(output));
            maxSlots = Math.max(maxSlots, inStacks.length);
        }
        if (outputs.isEmpty()) {
            return null;
        }
        if (outputs.size() == 1) {
            return new GuideAssemblyFactory(inputsPerRecipe.get(0), outputs.get(0), mjCosts.get(0));
        }
        ChangingItemStack[] input = new ChangingItemStack[maxSlots];
        for (int slot = 0; slot < maxSlots; slot++) {
            List<ItemStack> options = new ArrayList<>(outputs.size());
            for (ItemStack[] in : inputsPerRecipe) {
                options.add(slot < in.length ? in[slot] : ItemStack.EMPTY);
            }
            input[slot] = new ChangingItemStack(options);
        }
        ChangingItemStack output = new ChangingItemStack(outputs);
        ChangingObject<Long> mjCost = new ChangingObject<>(mjCosts.toArray(new Long[0]));
        return new GuideAssemblyFactory(input, output, mjCost);
    }
}
