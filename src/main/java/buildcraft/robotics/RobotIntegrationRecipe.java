/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.robotics;

import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.recipes.IngredientStack;
import buildcraft.api.recipes.IntegrationRecipe;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.recipe.IntegrationRecipeRegistry;
import buildcraft.robotics.boards.BCBoardNBT;
import buildcraft.robotics.item.ItemRobot;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class RobotIntegrationRecipe extends IntegrationRecipe {
   private static final long INTEGRATION_COST = 50000L * MjAPI.MJ;
   private static boolean initialized;

   public RobotIntegrationRecipe() {
      super(Identifier.fromNamespaceAndPath("buildcraftrobotics", "robot_integration"));
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         IntegrationRecipeRegistry.INSTANCE.addRecipe(new RobotIntegrationRecipe());
      }
   }

   @Override
   public ItemStack getOutput(@Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
      if (!(target.getItem() instanceof ItemRobot)) {
         return ItemStack.EMPTY;
      }

      RedstoneBoardRobotNBT boardNBT = null;

      for (ItemStack stack : toIntegrate) {
         if (!stack.isEmpty()) {
            if (!(stack.getItem() instanceof ItemRedstoneBoard)) {
               return ItemStack.EMPTY;
            }

            RedstoneBoardNBT<?> nbt = ItemRedstoneBoard.getBoardNBT(stack);
            if (!(nbt instanceof BCBoardNBT programmed)) {
               return ItemStack.EMPTY;
            }

            if (boardNBT != null) {
               return ItemStack.EMPTY;
            }

            boardNBT = programmed;
         }
      }

      if (boardNBT == null) {
         return ItemStack.EMPTY;
      }

      long energy = ItemRobot.getEnergy(target);
      if (energy <= 0L) {
         energy = EntityRobotBase.SAFETY_POWER;
      }

      return ItemRobot.createRobotStack(boardNBT, energy);
   }

   @Override
   public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
      return ImmutableList.of(new IngredientStack(Ingredient.of(BCRoboticsItems.REDSTONE_BOARD)));
   }

   @Override
   public long getRequiredMicroJoules(ItemStack output) {
      return INTEGRATION_COST;
   }

   @Override
   public IngredientStack getCenterStack() {
      return new IngredientStack(Ingredient.of(BCRoboticsItems.ROBOT));
   }
}
