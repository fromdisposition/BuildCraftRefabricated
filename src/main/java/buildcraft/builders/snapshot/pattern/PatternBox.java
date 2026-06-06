package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class PatternBox extends Pattern implements IFillerPatternShape {
   public PatternBox() {
      super("box");
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_BOX;
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      filledTemplate.setPlaneYZ(0, true);
      filledTemplate.setPlaneYZ(filledTemplate.getMax().getX(), true);
      filledTemplate.setPlaneXZ(0, true);
      filledTemplate.setPlaneXZ(filledTemplate.getMax().getY(), true);
      filledTemplate.setPlaneXY(0, true);
      filledTemplate.setPlaneXY(filledTemplate.getMax().getZ(), true);
      return true;
   }
}
