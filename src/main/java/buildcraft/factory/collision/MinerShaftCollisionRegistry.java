/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.collision;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MinerShaftCollisionRegistry {
   private static final Map<Level, Map<BlockPos, VoxelShape>> SHAPES = new WeakHashMap<>();

   private MinerShaftCollisionRegistry() {
   }

   public static void set(Level level, BlockPos pumpPos, @Nullable VoxelShape shape) {
      Map<BlockPos, VoxelShape> levelShapes = SHAPES.computeIfAbsent(level, ignored -> new WeakHashMap<>());
      if (shape == null || shape.isEmpty()) {
         levelShapes.remove(pumpPos);
      } else {
         levelShapes.put(pumpPos.immutable(), shape);
      }
   }

   public static void remove(Level level, BlockPos pumpPos) {
      Map<BlockPos, VoxelShape> levelShapes = SHAPES.get(level);
      if (levelShapes != null) {
         levelShapes.remove(pumpPos);
      }
   }

   public static Iterable<VoxelShape> concat(Level level, Iterable<VoxelShape> base, AABB box) {
      List<VoxelShape> extra = intersecting(level, box);
      return extra.isEmpty() ? base : Iterables.concat(base, extra);
   }

   private static List<VoxelShape> intersecting(Level level, AABB box) {
      Map<BlockPos, VoxelShape> levelShapes = SHAPES.get(level);
      if (levelShapes == null || levelShapes.isEmpty()) {
         return List.of();
      }

      List<VoxelShape> hits = new ArrayList<>();

      for (VoxelShape shape : levelShapes.values()) {
         if (!shape.isEmpty() && shape.bounds().intersects(box)) {
            hits.add(shape);
         }
      }

      return hits.isEmpty() ? List.of() : hits;
   }
}
