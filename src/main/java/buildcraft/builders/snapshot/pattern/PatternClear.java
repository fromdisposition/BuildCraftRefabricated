package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class PatternClear extends Pattern implements IFillerPatternShape {
   public PatternClear() {
      super("clear");
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_CLEAR;
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      return true;
   }
}
