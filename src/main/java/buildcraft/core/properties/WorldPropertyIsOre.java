/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.properties;

import buildcraft.api.core.IWorldProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WorldPropertyIsOre implements IWorldProperty {
   private final int harvestLevel;

   public WorldPropertyIsOre(int harvestLevel) {
      this.harvestLevel = harvestLevel;
   }

   @Override
   public boolean get(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      if (!this.isOre(state)) {
         return false;
      }

      return this.harvestLevel >= this.requiredLevel(state);
   }

   private boolean isOre(BlockState state) {
      if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
         return false;
      }

      Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
      String path = id.getPath();
      return path.contains("ore") || path.endsWith("_ore") || path.equals("ancient_debris");
   }

   private int requiredLevel(BlockState state) {
      if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
         return 3;
      } else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
         return 2;
      } else {
         return state.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0;
      }
   }

   @Override
   public void clear() {
   }
}
