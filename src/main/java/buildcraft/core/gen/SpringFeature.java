/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.gen;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.BCCoreBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SpringFeature extends Feature<NoneFeatureConfiguration> {
   public SpringFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      if (!EnumSpring.WATER.canGen) {
         return false;
      }

      BlockPos bedrock = SpringPlacement.findBedrock(level, origin.getX(), origin.getZ());
      if (bedrock == null) {
         return false;
      }

      level.setBlock(bedrock, BCCoreBlocks.SPRING_WATER.defaultBlockState(), 3);

      for (int y = bedrock.getY() + 1; y < level.getMaxY(); y++) {
         BlockPos column = new BlockPos(origin.getX(), y, origin.getZ());
         if (level.isEmptyBlock(column)) {
            break;
         }

         level.setBlock(column, Blocks.WATER.defaultBlockState(), 3);
      }

      return true;
   }
}
