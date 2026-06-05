/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.core.statements;

import net.minecraft.resources.Identifier;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

@SuppressWarnings({"this-escape", "unchecked"})
public abstract class BCStatement implements IStatement {

    protected final String uniqueTag;

    public BCStatement(String... uniqueTag) {
        this.uniqueTag = uniqueTag[0];
        for (String tag : uniqueTag) {
            StatementManager.statements.put(tag, this);
        }
    }

    @Override
    public String getUniqueTag() {
        return uniqueTag;
    }

    @Override
    public int maxParameters() {
        return 0;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public IStatement rotateLeft() {
        return this;
    }

    @Override
    public IStatement[] getPossible() {
        return new IStatement[] { this };
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return null;
    }

    protected static <P extends IStatementParameter> P getParam(int index, IStatementParameter[] params, P _default) {
        if (params == null || params.length <= index) {
            return _default;
        }
        IStatementParameter atIndex = params[index];
        if (atIndex != null && atIndex.getClass() == _default.getClass()) {
            return (P) atIndex;
        }
        return _default;
    }
}
