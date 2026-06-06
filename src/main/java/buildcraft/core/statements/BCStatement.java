package buildcraft.core.statements;

import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;

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
      return this.uniqueTag;
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
      return new IStatement[]{this};
   }

   @Override
   public IStatementParameter createParameter(int index) {
      return null;
   }

   protected static <P extends IStatementParameter> P getParam(int index, IStatementParameter[] params, P _default) {
      if (params != null && params.length > index) {
         IStatementParameter atIndex = params[index];
         return (P)(atIndex != null && atIndex.getClass() == _default.getClass() ? atIndex : _default);
      } else {
         return _default;
      }
   }
}
