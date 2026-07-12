/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.filler;

import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;
import javax.annotation.Nullable;

public interface IFillerPatternShape extends IFillerPattern {
   boolean fillTemplate(IFilledTemplate var1, IStatementParameter[] var2);

   @Nullable
   @Override
   default IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
      IFilledTemplate template = FillerManager.registry.createFilledTemplate(filler.getBox().min(), filler.getBox().size());
      return !this.fillTemplate(template, params) ? null : template;
   }
}
