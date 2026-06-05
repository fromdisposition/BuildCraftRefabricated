/* Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.statements;

public interface IStatement extends IGuiSlot {

    int maxParameters();

    int minParameters();

    IStatementParameter createParameter(int index);

    default IStatementParameter createParameter(IStatementParameter old, int index) {
        IStatementParameter _new = createParameter(index);
        if (old == null || _new == null) {
            return _new;
        } else if (old.getClass() == _new.getClass()) {
            return old;
        }
        return _new;
    }

    IStatement rotateLeft();

    IStatement[] getPossible();

    default boolean isPossibleOrdered() {
        return false;
    }
}
