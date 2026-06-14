/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.collision;

import buildcraft.factory.tile.TileMiner;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class MinerShaftCollisions {
   private MinerShaftCollisions() {
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

   public static List<AABB> sectionBoxes(BlockPos pumpPos, int lengthBlocks) {
      AABB fullBox = box(pumpPos, lengthBlocks);
      if (fullBox == null) {
         return List.of();
      }

      List<AABB> boxes = new ArrayList<>();
      double minY = fullBox.minY;
      while (minY < fullBox.maxY) {
         int sectionY = Math.floorDiv((int)Math.floor(minY), 16);
         double maxY = Math.min(fullBox.maxY, (sectionY + 1) * 16.0D);
         if (maxY > minY) {
            boxes.add(new AABB(fullBox.minX, minY, fullBox.minZ, fullBox.maxX, maxY, fullBox.maxZ));
         }

         minY = maxY;
      }

      return boxes;
   }
}
