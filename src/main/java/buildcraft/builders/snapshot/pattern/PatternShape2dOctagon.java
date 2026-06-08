/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.builders.BCBuildersSprites;

public class PatternShape2dOctagon extends PatternShape2d {
   public PatternShape2dOctagon() {
      super("2d_octagon");
   }

   @Override
   public int minParameters() {
      return 2;
   }

   @Override
   public int maxParameters() {
      return 2;
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.FILLER_2D_OCTAGON;
   }

   @Override
   protected void genShape(int maxA, int maxB, PatternShape2d.LineList list) {
   }
}
