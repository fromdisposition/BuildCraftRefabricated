/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.builders.BCBuildersSprites;

public class PatternShape2dPentagon extends PatternShape2d {
   private static final double DIST_HORIZONTAL = StrictMath.sin(Math.toRadians(18.0));
   private static final double DIST_VERTICAL;

   public PatternShape2dPentagon() {
      super("2d_pentagon");
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.FILLER_2D_PENTAGON;
   }

   @Override
   protected void genShape(int maxA, int maxB, PatternShape2d.LineList list) {
      int halfA = maxA / 2;
      int indentA = (int)Math.round(maxA * DIST_HORIZONTAL);
      int indentB = (int)Math.round(maxB * DIST_VERTICAL);
      list.moveTo(indentA, 0);
      list.lineTo(maxA - indentA, 0);
      list.lineFrom(maxA, indentB);
      list.lineTo(maxA - halfA, maxB);
      list.moveTo(halfA, maxB);
      list.lineFrom(0, indentB);
      list.lineTo(indentA, 0);
      list.setFillPoint(halfA, maxB / 2);
   }

   static {
      double cos54 = StrictMath.cos(Math.toRadians(54.0));
      double cos18 = StrictMath.cos(Math.toRadians(18.0));
      DIST_VERTICAL = cos54 / cos18;
   }
}
