package buildcraft.api.statements;

import net.minecraft.core.Direction;

public interface ITriggerExternalOverride {
   ITriggerExternalOverride.Result override(Direction var1, IStatementContainer var2, ITriggerExternal var3, IStatementParameter[] var4);

   enum Result {
      TRUE,
      FALSE,
      IGNORE;
   }
}
