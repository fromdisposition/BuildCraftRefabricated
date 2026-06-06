package buildcraft.api.statements;

public interface IStatement extends IGuiSlot {
   int maxParameters();

   int minParameters();

   IStatementParameter createParameter(int var1);

   default IStatementParameter createParameter(IStatementParameter old, int index) {
      IStatementParameter _new = this.createParameter(index);
      if (old != null && _new != null) {
         return old.getClass() == _new.getClass() ? old : _new;
      } else {
         return _new;
      }
   }

   IStatement rotateLeft();

   IStatement[] getPossible();

   default boolean isPossibleOrdered() {
      return false;
   }
}
