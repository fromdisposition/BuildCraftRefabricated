/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.builders.BCBuildersStatements;
import buildcraft.builders.snapshot.Snapshot;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.LocaleUtil;
import java.util.BitSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public abstract class Pattern extends BCStatement implements IFillerPattern, IActionExternal {
   private final String desc;

   public Pattern(String tag) {
      super("buildcraft:" + tag);
      this.desc = "fillerpattern." + tag;
      FillerManager.registry.addPattern(this);
   }

   @Override
   public String getDescription() {
      return LocaleUtil.localize(this.desc);
   }

   @Override
   public void actionActivate(BlockEntity target, Direction side, IStatementContainer source, IStatementParameter[] parameters) {
      if (source instanceof IFillerStatementContainer) {
         ((IFillerStatementContainer)source).setPattern(this, parameters);
      } else if (target instanceof IFillerStatementContainer) {
         ((IFillerStatementContainer)target).setPattern(this, parameters);
      }
   }

   @Override
   public IFillerPattern[] getPossible() {
      return BCBuildersStatements.PATTERNS;
   }

   protected static void fillEllipsoid(IFilledTemplate filledTemplate, Vec3 center, Vec3 radius, PatternParameterHollow hollow, Set<Direction> openSides) {
      BlockPos max = filledTemplate.getMax();
      BlockPos size = filledTemplate.getSize();
      boolean filledInner = hollow == PatternParameterHollow.FILLED_INNER;
      BitSet data = filledInner ? null : new BitSet(Snapshot.getDataSize(size));

      for (int x = 0; x <= max.getX(); x++) {
         double dx = Math.abs(x - center.x) / radius.x;
         double dxx = dx * dx;

         for (int y = 0; y <= max.getY(); y++) {
            double dy = Math.abs(y - center.y) / radius.y;
            double dyy = dy * dy;

            for (int z = 0; z <= max.getZ(); z++) {
               double dz = Math.abs(z - center.z) / radius.z;
               double dzz = dz * dz;
               if (dxx + dyy + dzz < 1.0) {
                  if (filledInner) {
                     filledTemplate.set(x, y, z, true);
                  } else {
                     data.set(Snapshot.posToIndex(size, x, y, z), true);
                  }
               }
            }
         }
      }

      if (filledInner) {
         return;
      }

      boolean outerFilled = hollow.outerFilled;

      for (int x = 0; x <= max.getX(); x++) {
         for (int y = 0; y <= max.getY(); y++) {
            if (!openSides.contains(Direction.NORTH)) {
               sweep(filledTemplate, data, outerFilled, x, y, 0, Direction.SOUTH, size.getZ());
            }

            if (!openSides.contains(Direction.SOUTH)) {
               sweep(filledTemplate, data, outerFilled, x, y, max.getZ(), Direction.NORTH, size.getZ());
            }
         }

         for (int z = 0; z <= max.getZ(); z++) {
            if (!openSides.contains(Direction.DOWN)) {
               sweep(filledTemplate, data, outerFilled, x, 0, z, Direction.UP, size.getY());
            }

            if (!openSides.contains(Direction.UP)) {
               sweep(filledTemplate, data, outerFilled, x, max.getY(), z, Direction.DOWN, size.getY());
            }
         }
      }

      for (int y = 0; y <= max.getY(); y++) {
         for (int z = 0; z <= max.getZ(); z++) {
            if (!openSides.contains(Direction.WEST)) {
               sweep(filledTemplate, data, outerFilled, 0, y, z, Direction.EAST, size.getX());
            }

            if (!openSides.contains(Direction.EAST)) {
               sweep(filledTemplate, data, outerFilled, max.getX(), y, z, Direction.WEST, size.getX());
            }
         }
      }
   }

   private static void sweep(IFilledTemplate filledTemplate, BitSet data, boolean outerFilled, int x, int y, int z, Direction dir, int count) {
      BlockPos size = filledTemplate.getSize();

      for (int i = 0; i < count; i++) {
         if (data.get(Snapshot.posToIndex(size, x, y, z))) {
            filledTemplate.set(x, y, z, true);
            return;
         }

         if (outerFilled) {
            filledTemplate.set(x, y, z, true);
         }

         x += dir.getStepX();
         y += dir.getStepY();
         z += dir.getStepZ();
      }
   }
}
