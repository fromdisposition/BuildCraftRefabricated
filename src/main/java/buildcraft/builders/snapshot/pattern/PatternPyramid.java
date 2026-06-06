package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterCenter;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterYDir;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import java.util.EnumMap;
import java.util.Map;

public class PatternPyramid extends Pattern implements IFillerPatternShape {
   private static final Map<PatternParameterCenter, PatternPyramid.PyramidDir> PYRAMID_DIRS = new EnumMap<>(PatternParameterCenter.class);

   public PatternPyramid() {
      super("pyramid");
   }

   public SpriteHolderRegistry.SpriteHolder getSprite() {
      return BCBuildersSprites.FILLER_PYRAMID;
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
      switch (index) {
         case 0:
            return PatternParameterYDir.UP;
         case 1:
            return PatternParameterCenter.CENTER;
         default:
            return null;
      }
   }

   @Override
   public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
      PatternPyramid.PyramidDir dir = params.length >= 2 && params[1] != null
         ? PYRAMID_DIRS.get((PatternParameterCenter)params[1])
         : PYRAMID_DIRS.get(PatternParameterCenter.CENTER);
      int stepY = params.length >= 1 && params[0] != null && !((PatternParameterYDir)params[0]).up ? -1 : 1;
      int y = stepY == 1 ? 0 : filledTemplate.getMax().getY();
      int xLower = 0;
      int xUpper = filledTemplate.getMax().getX();
      int zLower = 0;
      int zUpper = filledTemplate.getMax().getZ();

      while (y >= 0 && y <= filledTemplate.getMax().getY()) {
         filledTemplate.setAreaXZ(xLower, xUpper, y, zLower, zUpper, true);
         xLower += dir.xLowerDiff;
         xUpper += dir.xUpperDiff;
         zLower += dir.zLowerDiff;
         zUpper += dir.zUpperDiff;
         y += stepY;
         if (xLower > xUpper || zLower > zUpper) {
            break;
         }
      }

      return true;
   }

   static {
      for (PatternParameterCenter param : PatternParameterCenter.values()) {
         PYRAMID_DIRS.put(param, new PatternPyramid.PyramidDir(param));
      }
   }

   private static class PyramidDir {
      public final int xLowerDiff;
      public final int xUpperDiff;
      public final int zLowerDiff;
      public final int zUpperDiff;

      public PyramidDir(PatternParameterCenter param) {
         this.xLowerDiff = param.offsetX >= 0 ? 1 : 0;
         this.xUpperDiff = param.offsetX <= 0 ? -1 : 0;
         this.zLowerDiff = param.offsetZ >= 0 ? 1 : 0;
         this.zUpperDiff = param.offsetZ <= 0 ? -1 : 0;
      }
   }
}
