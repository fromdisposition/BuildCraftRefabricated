/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.collision;

import buildcraft.factory.tile.TileMiner;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MinerShaftCollisions {
   private MinerShaftCollisions() {
   }

   @Nullable
   public static VoxelShape shape(BlockPos pumpPos, int lengthBlocks) {
      AABB box = box(pumpPos, lengthBlocks);
      return box != null ? Shapes.create(box) : null;
   }

   @Nullable
   public static AABB box(BlockPos pumpPos, int lengthBlocks) {
      if (lengthBlocks <= 0) {
         return null;
      }

      int pumpY = pumpPos.getY();
      double minY = pumpY - lengthBlocks;
      double maxY = pumpY - TileMiner.SHAFT_TOP_INSET;
      if (maxY <= minY) {
         return null;
      }

      double centerX = pumpPos.getX() + 0.5D;
      double centerZ = pumpPos.getZ() + 0.5D;
      double half = TileMiner.SHAFT_CROSS_HALF;
      return new AABB(
         centerX - half,
         minY,
         centerZ - half,
         centerX + half,
         maxY,
         centerZ + half
      );
   }
}
