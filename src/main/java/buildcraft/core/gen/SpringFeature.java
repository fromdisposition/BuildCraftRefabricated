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

      int posX = origin.getX();
      int posZ = origin.getZ();

      for (int y = 0; y < 5; y++) {
         BlockPos pos = new BlockPos(posX, y, posZ);
         BlockState existing = level.getBlockState(pos);
         if (existing.getBlock() == Blocks.BEDROCK) {
            int placeY = y > 0 ? y : y - 1;
            if (placeY >= level.getMinY()) {
               BlockPos springPos = new BlockPos(posX, placeY, posZ);
               BlockState springState = BCCoreBlocks.SPRING_WATER.defaultBlockState();
               level.setBlock(springPos, springState, 3);

               for (int j = placeY + 1; j < level.getMaxY(); j++) {
                  BlockPos waterPos = new BlockPos(posX, j, posZ);
                  if (level.isEmptyBlock(waterPos)) {
                     break;
                  }

                  level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
               }

               return true;
            }
         }
      }

      return false;
   }
}
