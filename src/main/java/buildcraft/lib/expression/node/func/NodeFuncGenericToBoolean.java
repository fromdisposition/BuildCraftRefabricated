package buildcraft.lib.expression.node.func;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.INodeFunc;
import buildcraft.lib.expression.api.INodeStack;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class NodeFuncGenericToBoolean extends NodeFuncGeneric implements INodeFunc.INodeFuncBoolean {
   protected final IExpressionNode.INodeBoolean node;

   public NodeFuncGenericToBoolean(IExpressionNode.INodeBoolean node, Class<?>[] types, IVariableNode[] nodes) {
      super(node, types, nodes);
      this.node = node;
   }

   @Override
   public IExpressionNode.INodeBoolean getNode(INodeStack stack) throws InvalidExpressionException {
      return new NodeFuncGenericToBoolean.FuncBoolean(this.popArgs(stack));
   }

   protected class FuncBoolean extends NodeFuncGeneric.Func implements IExpressionNode.INodeBoolean {
      public FuncBoolean(IExpressionNode[] argsIn) {
         super(argsIn);
      }

      @Override
      public boolean evaluate() {
         this.setupEvaluate(this.realArgs);
         return NodeFuncGenericToBoolean.this.node.evaluate();
      }

      @Override
      public IExpressionNode.INodeBoolean inline() {
         IExpressionNode[] newArgs = new IExpressionNode[this.realArgs.length];
         NodeFuncGeneric.InlineType type = this.setupInline(newArgs);
         if (type == NodeFuncGeneric.InlineType.FULL) {
            this.setupEvaluate(newArgs);
            return NodeConstantBoolean.of(NodeFuncGenericToBoolean.this.node.evaluate());
         } else {
            return type == NodeFuncGeneric.InlineType.PARTIAL ? NodeFuncGenericToBoolean.this.new FuncBoolean(newArgs) : this;
         }
      }
   }
}
