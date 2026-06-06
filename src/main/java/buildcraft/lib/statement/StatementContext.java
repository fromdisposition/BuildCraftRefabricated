package buildcraft.lib.statement;

import buildcraft.api.statements.IGuiSlot;
import buildcraft.lib.gui.ISimpleDrawable;
import java.util.List;
import javax.annotation.Nullable;

public interface StatementContext<S extends IGuiSlot> {
   List<? extends StatementContext.StatementGroup<S>> getAllPossible();

   interface StatementGroup<S extends IGuiSlot> {
      List<S> getValues();

      @Nullable
      ISimpleDrawable getSourceIcon();

      default int getLedgerColour() {
         return 0;
      }
   }
}
