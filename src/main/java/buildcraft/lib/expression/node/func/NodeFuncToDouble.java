package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class NodeFuncToDouble implements INodeFunc.INodeFuncDouble, IExpressionNode.INodeDouble {
   private final String name;
   private final NodeFuncToDouble.IFuncToDouble func;

   public NodeFuncToDouble(String name, NodeFuncToDouble.IFuncToDouble func) {
      this.name = name;
      this.func = func;
   }

   @Override
   public double evaluate() {
      return this.func.apply();
   }

   @Override
   public IExpressionNode.INodeDouble inline() {
      return this;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      return this;
   }

   @Override
   public String toString() {
      return "[ -> double] { " + this.name + " }";
   }

   public interface IFuncToDouble {
      double apply();
   }
}
