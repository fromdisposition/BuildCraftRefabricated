package buildcraft.builders.snapshot.pattern;

import buildcraft.api.core.render.ISprite;
import buildcraft.builders.BCBuildersSprites;

public class PatternShape2dArc extends PatternShape2d {
   public PatternShape2dArc() {
      super("2d_arc");
   }

   @Override
   public ISprite getSprite() {
      return BCBuildersSprites.FILLER_2D_ARC;
   }

   @Override
   protected void genShape(int maxA, int maxB, PatternShape2d.LineList list) {
      if (maxA != 0 && maxB != 0) {
         list.setFillPoint(maxA, maxB);
         list.arc(maxA, maxB, maxA, maxB);
      } else {
         list.moveTo(0, 0);
         list.lineTo(maxA, maxB);
      }
   }
}
