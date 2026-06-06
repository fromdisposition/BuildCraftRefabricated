package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class PatternFill extends Pattern implements IFillerPatternShape {
   public PatternFill() {
      super("fill");
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_FILL;
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      filledTemplate.setAll(true);
      return true;
   }
}
