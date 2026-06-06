package buildcraft.lib.expression.node.value;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import java.util.Locale;

public abstract class NodeVariable implements IVariableNode {
   public final String name;
   protected boolean isConst = false;

   public NodeVariable(String name) {
      this.name = name.toLowerCase(Locale.ROOT);
   }

   @Override
   public void setConstant(boolean isConst) {
      this.isConst = isConst;
   }

   @Override
   public boolean isConstant() {
      return this.isConst;
   }

   public abstract void setConstantSource(IExpressionNode var1);

   @Override
   public String toString() {
      return "variable: " + this.name + " = " + this.evaluateAsString();
   }
}
