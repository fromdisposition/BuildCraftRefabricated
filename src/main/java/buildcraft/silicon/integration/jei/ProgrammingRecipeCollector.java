/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.integration.jei;

import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.ItemRedstoneBoard;
import buildcraft.silicon.tile.TileProgrammingTable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ProgrammingRecipeCollector {
   private ProgrammingRecipeCollector() {
   }

   public static List<ProgrammingRecipeJei> collect() {
      List<ProgrammingRecipeJei> out = new ArrayList<>();
      if (BuildcraftRecipeRegistry.programmingTable == null) {
         return out;
      }

      ItemStack blank = new ItemStack(BCRoboticsItems.REDSTONE_BOARD);

      for (IProgrammingRecipe recipe : BuildcraftRecipeRegistry.programmingTable.getRecipes()) {
         List<ItemStack> options = recipe.getOptions(TileProgrammingTable.WIDTH, TileProgrammingTable.HEIGHT);

         for (int i = 0; i < options.size(); i++) {
            ItemStack option = options.get(i);
            if (!option.isEmpty()) {
               out.add(
                  new ProgrammingRecipeJei(
                     recipe.getId() + ":" + boardId(option),
                     blank.copy(),
                     option.copy(),
                     i,
                     recipe.getEnergyCostMj(option)
                  )
               );
            }
         }
      }

      out.sort(Comparator.comparing(ProgrammingRecipeJei::id));
      return out;
   }

   private static String boardId(ItemStack option) {
      return ItemRedstoneBoard.getBoardNBT(option).getID();
   }
}
