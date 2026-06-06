package buildcraft.builders.filler;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import buildcraft.builders.snapshot.Template;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;

public class FillerUtil {
   public static Template.BuildingInfo createBuildingInfo(
      IFillerStatementContainer filler, FullStatement<IFillerPattern> patternStatement, IStatementParameter[] params, boolean inverted
   ) {
      Template.FilledTemplate filledTemplate = (Template.FilledTemplate)patternStatement.get().createTemplate(filler, params);
      if (filledTemplate == null) {
         return null;
      }

      if (inverted) {
         filledTemplate.getTemplate().invert();
      }

      return filledTemplate.getTemplate().new BuildingInfo(BlockPos.ZERO, Rotation.NONE);
   }
}
