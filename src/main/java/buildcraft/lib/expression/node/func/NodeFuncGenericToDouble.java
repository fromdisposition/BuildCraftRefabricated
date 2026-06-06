package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class NodeFuncGenericToDouble extends NodeFuncGeneric implements INodeFunc.INodeFuncDouble {
   protected final IExpressionNode.INodeDouble node;

   public NodeFuncGenericToDouble(IExpressionNode.INodeDouble node, Class<?>[] types, IVariableNode[] nodes) {
      super(node, types, nodes);
      this.node = node;
   }

   @Override
   public IExpressionNode.INodeDouble getNode(INodeStack stack) throws InvalidExpressionException {
      return new NodeFuncGenericToDouble.FuncDouble(this.popArgs(stack));
   }

   protected class FuncDouble extends NodeFuncGeneric.Func implements IExpressionNode.INodeDouble {
      public FuncDouble(IExpressionNode[] argsIn) {
         super(argsIn);
      }

      @Override
      public double evaluate() {
         this.setupEvaluate(this.realArgs);
         return NodeFuncGenericToDouble.this.node.evaluate();
      }

      @Override
      public IExpressionNode.INodeDouble inline() {
         IExpressionNode[] newArgs = new IExpressionNode[this.realArgs.length];
         NodeFuncGeneric.InlineType type = this.setupInline(newArgs);
         if (type == NodeFuncGeneric.InlineType.FULL) {
            this.setupEvaluate(newArgs);
            return new NodeConstantDouble(NodeFuncGenericToDouble.this.node.evaluate());
         } else {
            return type == NodeFuncGeneric.InlineType.PARTIAL ? NodeFuncGenericToDouble.this.new FuncDouble(newArgs) : this;
         }
      }
   }
}
