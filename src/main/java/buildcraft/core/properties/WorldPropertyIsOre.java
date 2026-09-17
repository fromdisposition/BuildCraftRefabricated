/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.properties;

import buildcraft.api.core.IWorldProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WorldPropertyIsOre implements IWorldProperty {
   private static final TagKey<Block> ORES = TagKey.create(Registries.BLOCK, Identifier.parse("c:ores"));
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

      return state.is(ORES) || state.is(Blocks.ANCIENT_DEBRIS);
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
