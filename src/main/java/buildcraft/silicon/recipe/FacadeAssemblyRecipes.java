/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.recipes.IngredientStack;

import buildcraft.lib.misc.ItemStackKey;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.recipe.ChangingItemStack;
import buildcraft.lib.recipe.ChangingObject;
import buildcraft.lib.recipe.IRecipeViewable;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeBlockStateInfo;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;
import buildcraft.silicon.plug.FacadeStateManager;

@SuppressWarnings("deprecation")
public class FacadeAssemblyRecipes extends AssemblyRecipe implements IRecipeViewable.IRecipePowered {
    public static final FacadeAssemblyRecipes INSTANCE = new FacadeAssemblyRecipes();

    static {
        INSTANCE.setRegistryName("buildcraftrefabricated:facadeRecipes");
    }

    private static final int TIME_GAP = 500;
    private static final long MJ_COST = 64 * MjAPI.MJ;
    private static final ChangingObject<Long> MJ_COSTS = new ChangingObject<>(new Long[] { MJ_COST });

    public static ItemStack createFacadeStack(FacadeBlockStateInfo info, boolean isHollow) {
        ItemStack stack = BCSiliconItems.PLUG_FACADE.get()
            .createItemStack(FacadeInstance.createSingle(info, isHollow));
        stack.setCount(6);
        return stack;
    }

    @Override
    public ChangingItemStack[] getRecipeInputs() {
        ChangingItemStack[] inputs = new ChangingItemStack[2];
        inputs[0] = new ChangingItemStack(baseRequirementStack());
        NonNullList<ItemStack> list = NonNullList.create();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                list.add(info.requiredStack);
                list.add(info.requiredStack);
            }
        }
        inputs[1] = new ChangingItemStack(list);
        inputs[1].setTimeGap(TIME_GAP);
        return inputs;
    }

    @Override
    public ChangingItemStack getRecipeOutputs() {
        NonNullList<ItemStack> list = NonNullList.create();
        for (FacadeBlockStateInfo info : FacadeStateManager.validFacadeStates.values()) {
            if (info.isVisible) {
                list.add(createFacadeStack(info, false));
                list.add(createFacadeStack(info, true));
            }
        }
        ChangingItemStack changing = new ChangingItemStack(list);
        changing.setTimeGap(TIME_GAP);
        return changing;
    }

    @Override
    public ChangingObject<Long> getMjCost() {
        return MJ_COSTS;
    }

    @Override
    public Set<ItemStack> getOutputs(NonNullList<ItemStack> inputs) {
        if (!StackUtil.contains(baseRequirementStack(), inputs)) {
            return Collections.emptySet();
        }

        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : inputs) {
            if (stack.isEmpty()) continue;
            stack = stack.copy();
            stack.setCount(1);
            ItemStackKey key = new ItemStackKey(stack);

            List<FacadeBlockStateInfo> infos = FacadeStateManager.stackFacades.get(key);
            if (infos != null && !infos.isEmpty()) {
                for (FacadeBlockStateInfo info : infos) {
                    stacks.add(createFacadeStack(info, false));
                    stacks.add(createFacadeStack(info, true));
                }
                continue;
            }

            List<FacadeBlockStateInfo> redirects = FacadeStateManager.stackRedirects.get(key);
            if (redirects != null) {
                for (FacadeBlockStateInfo redirect : redirects) {
                    stacks.add(createFacadeStack(redirect, false));
                    stacks.add(createFacadeStack(redirect, true));
                }
            }
        }

        java.util.TreeSet<ItemStack> set = new java.util.TreeSet<>((a, b) -> {
            if (ItemStack.isSameItemSameComponents(a, b)) return 0;

            net.minecraft.resources.Identifier thisId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(a.getItem());
            net.minecraft.resources.Identifier otherId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(b.getItem());
            int idCompare = thisId.compareTo(otherId);
            if (idCompare != 0) return idCompare;

            return a.getComponents().toString().compareTo(b.getComponents().toString());
        });
        set.addAll(stacks);

        return set;
    }

    private static ItemStack baseRequirementStack() {
        net.minecraft.world.item.Item pipe = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse("buildcrafttransport:pipe_structure"));
        if (pipe == Items.AIR) {
            return new ItemStack(Items.COBBLESTONE_WALL);
        }
        return new ItemStack(pipe, 3);
    }

    @Override
    public Set<ItemStack> getOutputPreviews() {
        return Collections.emptySet();
    }

    @Override
    public Set<IngredientStack> getInputsFor(@Nonnull ItemStack output) {
        FacadePhasedState state = ItemPluggableFacade.getStates(output).getCurrentStateForStack();
        FacadeBlockStateInfo targetInfo = state.stateInfo;
        ItemStack stateRequirement = targetInfo.requiredStack;

        if (stateRequirement.isEmpty()) {
            return Collections.emptySet();
        }

        List<net.minecraft.world.level.ItemLike> acceptedItems = new ArrayList<>();
        if (stateRequirement.getItem() != Items.AIR) {
            acceptedItems.add(stateRequirement.getItem());
        }
        for (java.util.Map.Entry<ItemStackKey, List<FacadeBlockStateInfo>> entry : FacadeStateManager.stackRedirects.entrySet()) {
            for (FacadeBlockStateInfo redirectInfo : entry.getValue()) {
                if (redirectInfo == targetInfo || redirectInfo.state == targetInfo.state) {
                    if (entry.getKey().baseStack.getItem() != Items.AIR) {
                        acceptedItems.add(entry.getKey().baseStack.getItem());
                    }
                    break;
                }
            }
        }

        if (acceptedItems.isEmpty()) {
            return Collections.emptySet();
        }

        Ingredient ingredientType = Ingredient.of(acceptedItems.toArray(new net.minecraft.world.level.ItemLike[0]));
        IngredientStack ingredientTypeStack = new IngredientStack(ingredientType);
        IngredientStack ingredientBase = new IngredientStack(Ingredient.of(baseRequirementStack().getItem()), 3);
        return ImmutableSet.of(ingredientTypeStack, ingredientBase);
    }

    @Override
    public long getRequiredMicroJoulesFor(@Nonnull ItemStack output) {
        return MJ_COST;
    }
}
