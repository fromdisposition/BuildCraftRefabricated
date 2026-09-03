/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterHollow;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PatternSphere extends Pattern implements IFillerPatternShape {
   public PatternSphere() {
      super("sphere");
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.FILLER_SPHERE;
   }

   @Override
   public int minParameters() {
      return 1;
   }

   @Override
   public int maxParameters() {
      return 1;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      switch (index) {
         case 0:
            return PatternParameterHollow.FILLED_INNER;
         default:
            return null;
      }
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      PatternParameterHollow hollow = getParam(0, params, PatternParameterHollow.FILLED_INNER);
      BlockPos max = filledTemplate.getMax();
      Vec3 center = new Vec3(max.getX() / 2.0, max.getY() / 2.0, max.getZ() / 2.0);
      fillEllipsoid(filledTemplate, center, center.add(0.5, 0.5, 0.5), hollow, EnumSet.noneOf(Direction.class));
      return true;
   }
}
