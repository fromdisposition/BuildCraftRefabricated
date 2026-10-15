/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
