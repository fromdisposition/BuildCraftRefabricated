package buildcraft.api.filler;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import javax.annotation.Nullable;

public interface IFillerPatternShape extends IFillerPattern {
   boolean fillTemplate(IFilledTemplate var1, IStatementParameter[] var2);

   @Nullable
   @Override
   default IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
      IFilledTemplate template = FillerManager.registry.createFilledTemplate(filler.getBox().min(), filler.getBox().size());
      return !this.fillTemplate(template, params) ? null : template;
   }
}
