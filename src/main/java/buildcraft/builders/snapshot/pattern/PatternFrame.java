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
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class PatternFrame extends Pattern implements IFillerPatternShape {
   public PatternFrame() {
      super("frame");
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      int maxX = filledTemplate.getMax().getX();
      int maxY = filledTemplate.getMax().getY();
      int maxZ = filledTemplate.getMax().getZ();
      filledTemplate.setLineX(0, maxX, 0, 0, true);
      filledTemplate.setLineX(0, maxX, maxY, 0, true);
      filledTemplate.setLineX(0, maxX, maxY, maxZ, true);
      filledTemplate.setLineX(0, maxX, 0, maxZ, true);
      filledTemplate.setLineY(0, 0, maxY, 0, true);
      filledTemplate.setLineY(maxX, 0, maxY, 0, true);
      filledTemplate.setLineY(maxX, 0, maxY, maxZ, true);
      filledTemplate.setLineY(0, 0, maxY, maxZ, true);
      filledTemplate.setLineZ(0, 0, 0, maxZ, true);
      filledTemplate.setLineZ(maxX, 0, 0, maxZ, true);
      filledTemplate.setLineZ(maxX, maxY, 0, maxZ, true);
      filledTemplate.setLineZ(0, maxY, 0, maxZ, true);
      return true;
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_FRAME;
   }
}
