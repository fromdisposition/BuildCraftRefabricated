package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToBoolean implements INodeFunc.INodeFuncBoolean, IExpressionNode.INodeBoolean {
   private final String name;
   private final NodeFuncToBoolean.IFuncToBoolean func;

   public NodeFuncToBoolean(String name, NodeFuncToBoolean.IFuncToBoolean func) {
      this.name = name;
      this.func = func;
   }

   @Override
   public boolean evaluate() {
      return this.func.apply();
   }

   @Override
   public IExpressionNode.INodeBoolean inline() {
      return this;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      return this;
   }

   @Override
   public String toString() {
      return "[ -> boolean] { " + this.name + " }";
   }

   public interface IFuncToBoolean {
      boolean apply();
   }
}
