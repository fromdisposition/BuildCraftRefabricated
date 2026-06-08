/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

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
