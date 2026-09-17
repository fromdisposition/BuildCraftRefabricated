/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.statement;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.lib.gui.ISimpleDrawable;

public interface IDrawingParameter extends IStatementParameter {
   ISimpleDrawable getDrawable();
}
