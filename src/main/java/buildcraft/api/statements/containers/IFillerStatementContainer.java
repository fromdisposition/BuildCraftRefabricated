/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements.containers;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import javax.annotation.Nullable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IFillerStatementContainer extends IStatementContainer {
   @Nullable
   @Override
   BlockEntity getTile();

   Level getFillerWorld();

   boolean hasBox();

   IBox getBox() throws IllegalStateException;

   void setPattern(IFillerPattern var1, IStatementParameter[] var2);
}
