package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantLong;

public class NodeFuncGenericToLong extends NodeFuncGeneric implements INodeFunc.INodeFuncLong {
   private final IExpressionNode.INodeLong node;

   public NodeFuncGenericToLong(IExpressionNode.INodeLong node, Class<?>[] types, IVariableNode[] nodes) {
      super(node, types, nodes);
      this.node = node;
   }

   @Override
   public IExpressionNode.INodeLong getNode(INodeStack stack) throws InvalidExpressionException {
      return new NodeFuncGenericToLong.FuncLong(this.popArgs(stack));
   }

   private class FuncLong extends NodeFuncGeneric.Func implements IExpressionNode.INodeLong {
      public FuncLong(IExpressionNode[] argsIn) {
         super(argsIn);
      }

      @Override
      public long evaluate() {
         this.setupEvaluate(this.realArgs);
         return NodeFuncGenericToLong.this.node.evaluate();
      }

      @Override
      public IExpressionNode.INodeLong inline() {
         IExpressionNode[] newArgs = new IExpressionNode[this.realArgs.length];
         NodeFuncGeneric.InlineType type = this.setupInline(newArgs);
         if (type == NodeFuncGeneric.InlineType.FULL) {
            this.setupEvaluate(newArgs);
            return new NodeConstantLong(NodeFuncGenericToLong.this.node.evaluate());
         } else {
            return type == NodeFuncGeneric.InlineType.PARTIAL ? NodeFuncGenericToLong.this.new FuncLong(newArgs) : this;
         }
      }
   }
}
