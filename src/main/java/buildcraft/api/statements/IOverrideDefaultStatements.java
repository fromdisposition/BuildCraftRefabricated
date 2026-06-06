package buildcraft.api.statements;

import java.util.List;

public interface IOverrideDefaultStatements {
   List<ITriggerExternal> overrideTriggers();

   List<IActionExternal> overrideActions();
}
