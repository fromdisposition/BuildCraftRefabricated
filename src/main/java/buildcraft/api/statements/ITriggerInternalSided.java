package buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface ITriggerInternalSided extends ITrigger {
   boolean isTriggerActive(Direction var1, IStatementContainer var2, IStatementParameter[] var3);
}
