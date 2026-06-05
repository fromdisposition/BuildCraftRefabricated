package buildcraft.lib.statement;

import java.util.List;

import javax.annotation.Nullable;

import buildcraft.api.statements.IGuiSlot;

import buildcraft.lib.gui.ISimpleDrawable;

public interface StatementContext<S extends IGuiSlot> {

    List<? extends StatementGroup<S>> getAllPossible();

    public interface StatementGroup<S extends IGuiSlot> {
        List<S> getValues();

        @Nullable
        ISimpleDrawable getSourceIcon();

        default int getLedgerColour() {
            return 0;
        }
    }
}
