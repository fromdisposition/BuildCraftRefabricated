/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.recipe;

import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.lib.misc.StackUtil;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class IntegrationRecipeBasic extends IntegrationRecipe {
   protected final long requiredMicroJoules;
   protected final IngredientStack target;
   protected final ImmutableList<IngredientStack> toIntegrate;
   @Nonnull
   protected final ItemStack output;

   public IntegrationRecipeBasic(
      Identifier name, long requiredMicroJoules, IngredientStack target, List<IngredientStack> toIntegrate, @Nonnull ItemStack output
   ) {
      super(name);
      this.requiredMicroJoules = requiredMicroJoules;
      this.target = target;
      this.toIntegrate = ImmutableList.copyOf(toIntegrate);
      this.output = output;
   }

   protected boolean matches(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
      if (!StackUtil.contains(this.target, target)) {
         return false;
      }

      NonNullList<ItemStack> toIntegrateCopy = toIntegrate.stream().filter(stack -> !stack.isEmpty()).collect(StackUtil.nonNullListCollector());
      boolean stackMatches = this.toIntegrate.stream().allMatch(definition -> {
         Iterator<ItemStack> iterator = toIntegrateCopy.iterator();

         while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (StackUtil.contains(definition, stack)) {
               iterator.remove();
               return true;
            }
         }

         return false;
      });
      return stackMatches && toIntegrateCopy.isEmpty();
   }

   @Override
   public ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
      return this.matches(target, toIntegrate) ? this.output : ItemStack.EMPTY;
   }

   @Override
   public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
      return this.toIntegrate;
   }

   @Override
   public long getRequiredMicroJoules(ItemStack output) {
      return this.requiredMicroJoules;
   }

   @Override
   public IngredientStack getCenterStack() {
      return this.target;
   }
}
