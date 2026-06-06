package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.ISimpleDrawable;

public interface IDrawingParameter extends IStatementParameter {
   ISimpleDrawable getDrawable();
}
