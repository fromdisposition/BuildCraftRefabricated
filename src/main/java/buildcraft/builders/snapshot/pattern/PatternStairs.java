/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterXZDir;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterYDir;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class PatternStairs extends Pattern implements IFillerPatternShape {
   public PatternStairs() {
      super("stairs");
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_STAIRS;
   }

   @Override
   public int maxParameters() {
      return 2;
   }

   @Override
   public int minParameters() {
      return 2;
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return index == 1 ? PatternParameterXZDir.EAST : PatternParameterYDir.UP;
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      PatternParameterYDir yDir = getParam(0, params, PatternParameterYDir.UP);
      PatternParameterXZDir xzDir = getParam(1, params, PatternParameterXZDir.EAST);
      int y = yDir == PatternParameterYDir.UP ? 0 : filledTemplate.getMax().getY();
      int yStep = yDir == PatternParameterYDir.UP ? 1 : -1;
      int yEnd = yDir == PatternParameterYDir.UP ? filledTemplate.getMax().getY() + 1 : -1;
      int fx = 0;
      int fz = 0;
      int tx = filledTemplate.getMax().getX();
      int tz = filledTemplate.getMax().getZ();

      while (y != yEnd) {
         filledTemplate.setAreaXZ(fx, tx, y, fz, tz, true);
         fx += xzDir.dir.getStepX() > 0 ? 1 : 0;
         fz += xzDir.dir.getStepZ() > 0 ? 1 : 0;
         tx += xzDir.dir.getStepX() < 0 ? -1 : 0;
         tz += xzDir.dir.getStepZ() < 0 ? -1 : 0;
         y += yStep;
         if (fx > tx || fz > tz) {
            break;
         }
      }

      return true;
   }
}
