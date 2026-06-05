/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.filler;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

public interface IFillerPattern extends IStatement {

    @Nullable
    IFilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params);

    @Override
    IFillerPattern[] getPossible();

    @Override
    ISprite getSprite();
}
