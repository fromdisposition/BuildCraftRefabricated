package buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface IActionInternalSided extends IAction {
   void actionActivate(Direction var1, IStatementContainer var2, IStatementParameter[] var3);
}
