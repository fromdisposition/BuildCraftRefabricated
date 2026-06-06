package buildcraft.api.gates;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.statements.containers.ISidedStatementContainer;
import buildcraft.api.transport.pipe.IPipeHolder;
import java.util.List;

public interface IGate extends ISidedStatementContainer {
   IPipeHolder getPipeHolder();

   List<IStatement> getTriggers();

   List<IStatement> getActions();

   List<StatementSlot> getActiveActions();

   List<IStatementParameter> getTriggerParameters(int var1);

   List<IStatementParameter> getActionParameters(int var1);
}
