package buildcraft.api.filler;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import javax.annotation.Nullable;

public interface IFillerPattern extends IStatement {
   @Nullable
   IFilledTemplate createTemplate(IFillerStatementContainer var1, IStatementParameter[] var2);

   IFillerPattern[] getPossible();

   @Override
   ISprite getSprite();
}
