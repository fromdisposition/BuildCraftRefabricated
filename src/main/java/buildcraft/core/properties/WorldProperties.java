/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.properties;

import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IWorldProperty;
import buildcraft.api.crops.CropManager;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class WorldProperties {
   public static final int MAX_HARVEST_LEVEL = 3;

   private WorldProperties() {
   }

   public static void register() {
      reg("replaceable", (world, pos) -> world.getBlockState(pos).canBeReplaced());
      reg("soft", WorldProperties::isSoft);
      reg("wood", tag(BlockTags.LOGS));
      reg("leaves", tag(BlockTags.LEAVES));
      reg("dirt", tag(BlockTags.DIRT));
      reg("shoveled", tag(BlockTags.MINEABLE_WITH_SHOVEL));
      reg("farmland", (world, pos) -> world.getBlockState(pos).is(Blocks.FARMLAND));
      reg("harvestable", (world, pos) -> CropManager.isMature(world, world.getBlockState(pos), pos));
      reg("fluidSource", WorldProperties::isFluidSource);

      for (int i = 0; i <= MAX_HARVEST_LEVEL; i++) {
         BuildCraftAPI.registerWorldProperty("ore@hardness=" + i, new WorldPropertyIsOre(i));
      }
   }

   private static void reg(String name, BiPredicate<Level, BlockPos> predicate) {
      BuildCraftAPI.registerWorldProperty(name, new IWorldProperty() {
         @Override
         public boolean get(Level world, BlockPos pos) {
            return predicate.test(world, pos);
         }

         @Override
         public void clear() {
         }
      });
   }

   private static BiPredicate<Level, BlockPos> tag(TagKey<Block> tag) {
      return (world, pos) -> world.getBlockState(pos).is(tag);
   }

   private static boolean isSoft(Level world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.isAir() || BuildCraftAPI.softBlocks.contains(state.getBlock()) || state.canBeReplaced();
   }

   private static boolean isFluidSource(Level world, BlockPos pos) {
      FluidState fluidState = world.getFluidState(pos);
      return !fluidState.isEmpty() && fluidState.isSource();
   }
}
